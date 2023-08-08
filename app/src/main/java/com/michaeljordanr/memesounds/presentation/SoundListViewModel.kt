package com.michaeljordanr.memesounds.presentation

import android.app.Application
import android.media.SoundPool
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.michaeljordanr.memesounds.model.Audio
import com.michaeljordanr.memesounds.Utils
import com.michaeljordanr.memesounds.db.BookmarkDao
import com.michaeljordanr.memesounds.db.BookmarkEntity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundListViewModel @Inject constructor(
    private val application: Application,
    private val dao: BookmarkDao
) : AndroidViewModel(application) {

    private lateinit var jsonAdapter: JsonAdapter<List<Audio>>
    private val moshi by lazy { Moshi.Builder().build() }
    private val typeData by lazy { Types.newParameterizedType(List::class.java, Audio::class.java) }

    private val _soundListState = mutableStateListOf<Audio>()
    private val _bookmarkListState = mutableStateListOf<Audio>()

    val soundListFilteredState = mutableStateListOf<Audio>()
    val bookmarkListFilteredState = mutableStateListOf<Audio>()

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    var audioSelected: Audio? = null

    private var streamingId = 0
    private var soundPool: SoundPool? = null

    init {
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        reload()
    }

    private fun reload() {
        _soundListState.clear()
        _soundListState.addAll(getAudioList())
        soundListFilteredState.clear()
        soundListFilteredState.addAll(getAudioList())
        getBookmarkList()
    }

    private fun getAudioList(): List<Audio> {
        jsonAdapter = moshi.adapter(typeData)
        val jsonString = Utils.loadJSONFromAsset(application, "config.json")

        return jsonAdapter.fromJson(jsonString.toString()) ?: listOf()
    }

    fun play(audioName: String) {
        application.baseContext?.let { context ->
            if (streamingId > 0) {
                soundPool?.stop(streamingId)
            }

            val id = Utils.getAudioIdFromRaw(context, audioName)
            val soundDescriptor = context.resources.openRawResourceFd(id)
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

    private var searchJob: Job? = null

    fun onSearch(query: String) {
        _searchQuery.value = query

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (query.isNotBlank()) {
                soundListFilteredState.clear()
                soundListFilteredState.addAll(_soundListState.filter {
                    it.audioDescription.uppercase().contains(query.uppercase().trim())
                })

                bookmarkListFilteredState.clear()
                bookmarkListFilteredState.addAll(_bookmarkListState.filter {
                    it.audioDescription.uppercase().contains(
                        query.uppercase().trim()
                    )
                })
            } else {
                reload()
            }
        }
    }

    fun share() {
        application.baseContext?.let {
            audioSelected?.let { audioSelected ->
                Utils.shareAudio(it, audioSelected)
            }
        }
    }

    fun bookmark() {
        viewModelScope.launch {
            audioSelected?.let { audio ->
                dao.insertBookmark(BookmarkEntity(id = audio.id, audioName = audio.audioName))
                getBookmarkList()
            }
        }
    }

    fun unbookmark() {
        viewModelScope.launch {
            audioSelected?.let { audio -> dao.deleteBookmark(audio.id) }
            getBookmarkList()
        }
    }

    private fun getBookmarkList() {
        viewModelScope.launch {
            val bookmarks = dao.getBookmarks()
            val audioBookmarkList = mutableListOf<Audio>()
            getAudioList().forEach { audio ->
                bookmarks.forEach { bookmark ->
                    if (audio.id == bookmark.id) {
                        audioBookmarkList.add(audio)
                    }
                }
            }

            _bookmarkListState.clear()
            _bookmarkListState.addAll(audioBookmarkList)
            bookmarkListFilteredState.clear()
            bookmarkListFilteredState.addAll(audioBookmarkList)
        }
    }
}