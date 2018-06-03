package com.michaeljordanr.memesounds;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AudioAsyncTask extends AsyncTask<String, Void, List<Audio>> {

    public interface AudioAsyncTaskListener {
        void onCompleteTask(List<Audio> audioList);
    }

    private Context context;
    private AudioAsyncTaskListener listener;
    private ProgressDialog progressDialog;
    List<Audio> audioList;

    public AudioAsyncTask(Context context, AudioAsyncTaskListener listener){
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context, R.style.Progress_Dialog_Theme);
        progressDialog.setTitle(context.getString(R.string.loading));
        progressDialog.setMessage(context.getString(R.string.loading_msg));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    protected List<Audio> doInBackground(String... strings) {

        String jsonSource = Utils.loadJSONFromAsset(context,"config.json");
        Type type = new TypeToken<List<Audio>>() {
        }.getType();
        audioList = new Gson().fromJson(jsonSource, type);

        return audioList;
    }

    @Override
    protected void onPostExecute(List<Audio> audios) {
        super.onPostExecute(audios);
        progressDialog.dismiss();
        listener.onCompleteTask(audioList);
    }
}
