package com.michaeljordanr.memesounds

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flurry.android.FlurryAgent
import com.github.piasy.rxandroidaudio.PlayConfig
import com.github.piasy.rxandroidaudio.RxAudioPlayer
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microsoft.appcenter.analytics.Analytics
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize

class AudioListFragment : Fragment(), AudioAdapter.RecyclerAdapterOnClickListener,
        AudioAdapter.RecyclerAdapterOnLongListener, BottomSheetFragment.BottomSheetFragmentListener {

    private lateinit var baseContext: Context
    private var rxAudioPlayer: RxAudioPlayer? = null
    private var adapter: AudioAdapter? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var activityListener: AudioListFragmentListener

    private val bottomSheetFragment by lazy { BottomSheetFragment(this) }

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
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audio_list, container, false)
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        audio.audioDescription.toUpperCase().contains(query.toUpperCase())
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

            rxAudioPlayer = RxAudioPlayer.getInstance()

            val openAppEvent = "open_app"
            firebaseAnalytics = FirebaseAnalytics.getInstance(it)
            firebaseAnalytics.logEvent(openAppEvent, null)

            FlurryAgent.logEvent(openAppEvent)
            Analytics.trackEvent(openAppEvent)
        }
    }

    private fun getAudioList(): List<Audio> {
        val jsonSource = Utils.loadJSONFromAsset(baseContext, MainActivity.JSON_CONFIG_PATH)
        val type = object : TypeToken<List<Audio>>() {}.type
        return Gson().fromJson<List<Audio>>(jsonSource, type)
    }

    override fun onClick(audio: Audio) {
        val audioName = audio.audioName
        play(audioName)

        val params = Bundle()
        val playLog = "play"
        params.putString("audio_name", audioName)
        firebaseAnalytics.logEvent(playLog, params)

        val logParams = HashMap<String, String>()
        logParams["audio_name"] = audioName
        FlurryAgent.logEvent(playLog, logParams)
        Analytics.trackEvent(playLog, logParams)
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
            val audioLoaded =
                    PlayConfig.res(it, Utils.getAudioIdFromRaw(it, audioName))
                            .looping(false) // loop or not
                            .leftVolume(1.0f) // left volume
                            .rightVolume(1.0f) // right volume
                            .build() // build this config and play!

            rxAudioPlayer!!.play(audioLoaded)
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : Observer<Boolean> {
                        override fun onComplete() {}

                        override fun onSubscribe(d: Disposable) {}

                        override fun onNext(t: Boolean) {}

                        override fun onError(e: Throwable) {}

                    })
        }
    }

    private fun sharedPreferencesObj(): SharedPreferences? {
        return context?.getSharedPreferences("MEMESOUNDS_SHARED_PREFERENCES", Context.MODE_PRIVATE)
    }

    private fun getBookmarks(): ArrayList<Audio> {
        sharedPreferencesObj()?.let { prefs ->
            return Gson().fromJson(
                    prefs.getString(BOOKMARKS_AUDIO_KEY, "") ?: "",
                    object : TypeToken<ArrayList<Audio>>() {}.type
            ) ?: arrayListOf()
        } ?: kotlin.run {
            return arrayListOf()
        }
    }

    override fun onFinished() {
        if (bottomSheetFragment.isAdded) {
            bottomSheetFragment.dismiss()
        }
    }

    override fun unbookmark(id: Int) {
        sharedPreferencesObj()?.let { prefs ->
            val editor = prefs.edit()
            var bookmarks = getBookmarks()
            bookmarks = ArrayList(bookmarks.filter { audio -> audio.id != id })
            editor.putString(BOOKMARKS_AUDIO_KEY, Gson().toJson(bookmarks))
            editor.apply()

            adapter?.setData(getBookmarks())
        }
    }

    override fun bookmark(audio: Audio) {
        sharedPreferencesObj()?.let { prefs ->
            val editor = prefs.edit()
            val bookmarks = getBookmarks()
            bookmarks.add(audio)
            editor.putString(BOOKMARKS_AUDIO_KEY, Gson().toJson(bookmarks))
            editor.apply()

            Handler().postDelayed({
                activityListener.goToPage(AudioListType.FAVORITES)
            }, 500)
        }
    }
}