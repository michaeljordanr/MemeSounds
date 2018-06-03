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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements AudioAdapter.RecyclerAdapterOnClickListener,
        AudioAdapter.RecyclerAdapterOnLongListener, AudioAsyncTask.AudioAsyncTaskListener{

    private SoundManager soundManager;
    int maxSimultaneousStreams = 3;
    private static final int PERMISSION_REQUEST = 333;

    private AudioAdapter adapter;
    Audio audioTemp;

    @BindView(R.id.rv_buttons)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        new AudioAsyncTask(this, this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (soundManager == null) {
            new AudioAsyncTask(this, this).execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (soundManager != null) {
            soundManager.cancel();
            soundManager = null;
        }
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



    @Override
    public void onClick(Audio audio) {
        int resourceId = Utils.getRawId(this, audio.getAudioName());
        soundManager.load(resourceId);
        soundManager.play(resourceId);
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


    @Override
    public void onCompleteTask(List<Audio> audioList) {
        soundManager = new SoundManager(this, maxSimultaneousStreams);
        soundManager.start();

        adapter = new AudioAdapter(this, this, this, soundManager);
        adapter.setData(audioList);

        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
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
