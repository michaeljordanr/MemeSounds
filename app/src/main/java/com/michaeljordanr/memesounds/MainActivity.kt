package com.michaeljordanr.memesounds

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import com.github.piasy.rxandroidaudio.PlayConfig
import com.github.piasy.rxandroidaudio.RxAudioPlayer
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity(), AudioAdapter.RecyclerAdapterOnClickListener, AudioAdapter.RecyclerAdapterOnLongListener {

    private var rxAudioPlayer: RxAudioPlayer? = null
    private var adapter: AudioAdapter? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.app_name)
        }

        val jsonSource = Utils.loadJSONFromAsset(this, JSON_CONFIG_PATH)
        val type = object : TypeToken<List<Audio>>() {

        }.type
        val audioList = Gson().fromJson<List<Audio>>(jsonSource, type)

        adapter = AudioAdapter(this, this, this)
        adapter!!.setData(audioList)

        val layoutManager = GridLayoutManager(this, 3)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_buttons)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        rxAudioPlayer = RxAudioPlayer.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.search)

        val searchView = searchItem.actionView as SearchView
        searchView.isFocusable = false
        searchView.queryHint = getText(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                adapter!!.filter.filter(query)
                if (query == "") {
                    searchView.isIconified = true
                }

                val params = Bundle()
                params.putString("query", query)
                firebaseAnalytics.logEvent("filter", params)

                return false
            }
        })
        return true
    }

    override fun onClick(audio: Audio) {
        play(audio.audioName)
    }

    override fun onLongClick(audio: Audio) {
        shareAudio(audio)
    }

    private fun play(audioName: String) {
        val audioLoaded = PlayConfig.file(Utils.getFileAudio(this, audioName))
                //PlayConfig.res(getApplicationContext(), resourceId) // or play a raw resource
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

        val params = Bundle()
        params.putString("audio_name", audioName)
        firebaseAnalytics.logEvent("play", params)
    }

    private fun shareAudio(audio: Audio) {
        val uri = Uri.parse(uriAssetsProvider + audio.audioName + Utils.AUDIO_FORMAT)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "audio/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(
                intent,
                resources.getString(R.string.share_audio)
                        + " \"" + audio.audioDescription + "\"")
        )

        val params = Bundle()
        params.putString("audio_name", audio.audioName)
        firebaseAnalytics.logEvent("share_audio", params)
    }

    companion object {
        private const val uriAssetsProvider = "content://" + BuildConfig.APPLICATION_ID + "/"
        private const val JSON_CONFIG_PATH = "config.json"
    }

}
