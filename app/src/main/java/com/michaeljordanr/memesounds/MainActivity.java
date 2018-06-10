package com.michaeljordanr.memesounds;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;


import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements AudioAdapter.RecyclerAdapterOnClickListener,
        AudioAdapter.RecyclerAdapterOnLongListener{

    private static final int PERMISSION_REQUEST = 333;

    private RxAudioPlayer rxAudioPlayer;

    private List<Audio> audioList;
    private AudioAdapter adapter;
    private Audio audioTemp;

    @BindView(R.id.rv_buttons)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        String jsonSource = Utils.loadJSONFromAsset(this,"config.json");
        Type type = new TypeToken<List<Audio>>() {
        }.getType();
        audioList = new Gson().fromJson(jsonSource, type);

        adapter = new AudioAdapter(this, this, this);
        adapter.setData(audioList);

        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        rxAudioPlayer = RxAudioPlayer.getInstance();
    }


    @Override
    public void onClick(Audio audio) {
        int resourceId = Utils.getRawId(this, audio.getAudioName());

        play(resourceId);
    }

    @Override
    public void onLongClick(Audio audio) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "Permissão necessária para o compartilhamento de audio",
                        Toast.LENGTH_SHORT).show();
            } else {
                audioTemp = audio;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST);

            }

        } else {
            shareAudio(Utils.getRawId(this, audio.getAudioName()));
        }
    }

    private void play(int resourceId){
        PlayConfig audioLoaded = PlayConfig.res(getApplicationContext(), resourceId) // or play a raw resource
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

    private void shareAudio(int audio){
        InputStream inputStream;
        FileOutputStream fileOutputStream;
        try {
            inputStream = getResources().openRawResource(audio);
            fileOutputStream = new FileOutputStream(
                    new File(Environment.getExternalStorageDirectory(), "sound.mp3"));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,
                Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/sound.mp3" ));
        intent.setType("audio/*");
        startActivity(Intent.createChooser(intent, "Share sound"));
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareAudio(Utils.getRawId(this, audioTemp.getAudioName()));
                } else {
                    Toast.makeText(this, "Permissão não concedida", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}
