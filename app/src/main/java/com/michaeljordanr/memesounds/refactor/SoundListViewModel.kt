package com.michaeljordanr.memesounds.refactor

import android.app.Application
import android.content.res.AssetManager
import android.media.SoundPool
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import com.michaeljordanr.memesounds.Audio
import com.michaeljordanr.memesounds.MainActivity
import com.michaeljordanr.memesounds.Utils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class SoundListViewModel(private val application: Application) : AndroidViewModel(application) {

    private lateinit var jsonAdapter: JsonAdapter<List<Audio>>
    private val moshi by lazy { Moshi.Builder().build() }
    private val typeData by lazy { Types.newParameterizedType(List::class.java, Audio::class.java) }

    private val _soundListState = mutableStateListOf<Audio>()
    val soundListState: SnapshotStateList<Audio> = _soundListState

    private var streamingId = 0
    private var soundPool: SoundPool? = null
    private var assetManager: AssetManager? = null

    init {
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        assetManager = application.baseContext?.assets

        _soundListState.addAll(getAudioList())
    }

    private fun getAudioList(): List<Audio> {
        jsonAdapter = moshi.adapter(typeData)
        val jsonString = Utils.loadJSONFromAsset(application, MainActivity.JSON_CONFIG_PATH)

        return jsonAdapter.fromJson(jsonString.toString()) ?: listOf()
    }

    fun play(audioName: String) {
        application.baseContext?.let {
            if (streamingId > 0) {
                soundPool?.stop(streamingId)
            }

            val soundDescriptor = assetManager?.openFd(audioName + Utils.AUDIO_FORMAT)
            val soundId = soundPool?.load(soundDescriptor, 1) ?: 0
            soundPool?.setOnLoadCompleteListener { _, _, _ ->
                streamingId = soundPool?.play(
                    soundId,
                    1.0f,
                    1.0f,
                    1,
                    0,
                    1.0f
                ) ?: 0
            }
        }
    }

    fun share(audio: Audio) {
        application.baseContext?.let {
            Utils.shareAudio(it, audio)
        }
    }
}