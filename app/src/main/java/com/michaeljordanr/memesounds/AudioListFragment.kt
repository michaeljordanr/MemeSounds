package com.michaeljordanr.memesounds

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michaeljordanr.memesounds.refactor.Utils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.parcelize.Parcelize
import java.text.Normalizer
import java.util.Locale

class AudioListFragment : Fragment(),
        AudioAdapter.RecyclerAdapterOnClickListener,
        AudioAdapter.RecyclerAdapterOnLongListener,
        BottomSheetFragment.BottomSheetFragmentListener {

    private lateinit var baseContext: Context
    private var adapter: AudioAdapter? = null
    private lateinit var activityListener: AudioListFragmentListener

    private val bottomSheetFragment by lazy { BottomSheetFragment(this) }
    private val moshi by lazy { Moshi.Builder().build() }
    private val typeData by lazy { Types.newParameterizedType(List::class.java, Audio::class.java) }

    private lateinit var jsonAdapter: JsonAdapter<List<Audio>>

    private var customApplication: CustomApplication? = null
    private var streamingId = 0
    private var soundPool: SoundPool? = null
    private var assetManager : AssetManager? = null

    @Parcelize
    enum class AudioListType(val value: Int) : Parcelable {
        ALL(0), FAVORITES(1)
    }

    interface AudioListFragmentListener {
        fun goToPage(audioListType: AudioListType)
    }

    companion object {
        const val AUDIO_LIST_TYPE = "audio_list_type"
        const val AUDIO_LIST_QUERY = "audio_list_query"

        const val BOOKMARKS_AUDIO_KEY = "bookmark_audio_key"

        fun newInstance(type: AudioListType, query: String = "") = AudioListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(AUDIO_LIST_TYPE, type)
                putString(AUDIO_LIST_QUERY, query)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseContext = context
        if (context is AudioListFragmentListener) {
            activityListener = context
            customApplication = activity?.applicationContext as? CustomApplication
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audio_list, container, false)
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jsonAdapter = moshi.adapter(typeData)

        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        assetManager = context?.assets

        context?.let {
            var audioList = getAudioList()

            arguments?.let { bundle ->
                val filterType = bundle.getParcelable<AudioListType>(AUDIO_LIST_TYPE)
                val query = bundle.getString(AUDIO_LIST_QUERY) ?: ""

                filterType?.let {
                    if (filterType == AudioListType.FAVORITES) {
                        audioList = getBookmarks()
                    }
                }

                if (query.isNotBlank()) {
                    audioList = audioList.filter { audio ->
                        audio.audioDescription.unaccent().uppercase(Locale.getDefault())
                            .contains(query.unaccent().uppercase(Locale.getDefault()))
                    }
                }
            }

            adapter = AudioAdapter(it, this, this)
            adapter?.setData(audioList)

            val layoutManager = GridLayoutManager(it, 3)
            val recyclerView = view.findViewById<RecyclerView>(R.id.rv_buttons)
            recyclerView.layoutManager = layoutManager
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter
        }
    }

    private fun getAudioList(): List<Audio> {
        val jsonString = Utils.loadJSONFromAsset(baseContext, MainActivity.JSON_CONFIG_PATH)

        return jsonAdapter.fromJson(jsonString.toString()) ?: listOf()
    }

    override fun onClick(audio: Audio) {
        val audioName = audio.audioName
        play(audioName)

        customApplication?.let {
            val params = HashMap<String, String>()
            params["audio_name"] = audioName

            it.sendAnalytics("play", params)
        }
    }

    override fun onLongClick(audio: Audio) {
        val parent = activity as? MainActivity

        parent?.let {
            bottomSheetFragment.setData(audio, it.selectedTab)
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun play(audioName: String) {
        context?.let {
            if (streamingId > 0) {
                soundPool?.stop(streamingId)
            }

            val soundDescriptor = assetManager?.openFd(audioName + Utils.AUDIO_FORMAT)
            val soundId = soundPool?.load(soundDescriptor, 1) ?: 0
            soundPool?.setOnLoadCompleteListener { _, _, _ ->
                streamingId = soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) ?: 0
            }
        }
    }

    private fun sharedPreferencesObj(): SharedPreferences? {
        return context?.getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE)
    }

    private fun getBookmarks(): ArrayList<Audio> {
        sharedPreferencesObj()?.let { prefs ->
            val jsonString = prefs.getString(BOOKMARKS_AUDIO_KEY, "") ?: ""
            return if (jsonString.isNotEmpty()) {
                ArrayList(jsonAdapter.fromJson(jsonString) ?: listOf())
            } else {
                arrayListOf()
            }
        } ?: kotlin.run {
            return arrayListOf()
        }
    }

    override fun onFinished() {
        if (bottomSheetFragment.isAdded) {
            bottomSheetFragment.dismiss()
        }
    }

    override fun unbookmark(audio: Audio) {
        sharedPreferencesObj()?.let { prefs ->
            val editor = prefs.edit()
            var bookmarks = getBookmarks()
            bookmarks = ArrayList(bookmarks.filter { b -> b.id != audio.id })
            editor.putString(BOOKMARKS_AUDIO_KEY, jsonAdapter.toJson(bookmarks))
            editor.apply()

            adapter?.setData(getBookmarks())

            customApplication?.let { app ->
                val params = HashMap<String, String>()
                params["audio_name"] = audio.audioName

                app.sendAnalytics("unbookmark", params)
            }
        }
    }

    override fun bookmark(audio: Audio) {
        sharedPreferencesObj()?.let { prefs ->
            val editor = prefs.edit()
            var bookmarks = getBookmarks()
            bookmarks.add(audio)
            bookmarks = ArrayList(bookmarks.distinctBy { it.id })
            editor.putString(BOOKMARKS_AUDIO_KEY, jsonAdapter.toJson(bookmarks))
            editor.apply()

            customApplication?.let { app ->
                val params = HashMap<String, String>()
                params["audio_name"] = audio.audioName

                app.sendAnalytics("bookmark", params)
            }

            Handler().postDelayed({
                activityListener.goToPage(AudioListType.FAVORITES)
            }, 500)
        }
    }

    override fun onStop() {
        super.onStop()
        soundPool?.stop(streamingId)
    }
}

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}