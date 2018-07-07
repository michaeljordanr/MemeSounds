package com.michaeljordanr.memesounds;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements AudioAdapter.RecyclerAdapterOnClickListener,
        AudioAdapter.RecyclerAdapterOnLongListener{

    private static final String uriAssetsProvider = "content://" + BuildConfig.APPLICATION_ID + "/";
    private static final String JSON_CONFIG_PATH = "config.json";

    private RxAudioPlayer rxAudioPlayer;
    private AudioAdapter adapter;

    @BindView(R.id.rv_buttons)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        String jsonSource = Utils.loadJSONFromAsset(this,JSON_CONFIG_PATH);
        Type type = new TypeToken<List<Audio>>() {
        }.getType();
        List<Audio> audioList = new Gson().fromJson(jsonSource, type);

        adapter = new AudioAdapter(this, this, this);
        adapter.setData(audioList);

        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        rxAudioPlayer = RxAudioPlayer.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setFocusable(false);
        searchView.setQueryHint(getText(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                if(query.equals("")){
                    searchView.setIconified(true);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public void onClick(Audio audio) {
        play(audio.getAudioName());
    }

    @Override
    public void onLongClick(Audio audio) {
        shareAudio(audio);
    }

    private void play(String audioName){
        PlayConfig audioLoaded = PlayConfig.file(Utils.getFileAudio(this, audioName))
                //PlayConfig.res(getApplicationContext(), resourceId) // or play a raw resource
                .looping(false) // loop or not
                .leftVolume(1.0F) // left volume
                .rightVolume(1.0F) // right volume
                .build(); // build this config and play!

        rxAudioPlayer.play(audioLoaded)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(final Disposable disposable) { }

                    @Override
                    public void onNext(final Boolean aBoolean) { }

                    @Override
                    public void onError(final Throwable throwable) { }

                    @Override
                    public void onComplete() { }
                });
    }

    private void shareAudio(Audio audio){
        Uri uri = Uri.parse(uriAssetsProvider + audio.getAudioName() + Utils.AUDIO_FORMAT);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(
                intent,
                getResources().getString(R.string.share_audio)
                + " \"" + audio.getAudioDescription() + "\"")
        );
    }

}
