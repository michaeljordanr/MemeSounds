package com.michaeljordanr.memesounds;

import android.content.Context;
import android.content.res.AssetManager;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static String AUDIO_FORMAT = ".mp3";

    public static int getDrawableId(Context c, String imageName) {
        String packageName = c.getApplicationContext().getPackageName();
        return c.getResources().getIdentifier(imageName, "drawable", packageName);
    }

    public static File getFileAudio(Context c, String audio) {
        try {
            AssetManager am = c.getAssets();
            InputStream in = am.open(audio + AUDIO_FORMAT);
            File tempFile = File.createTempFile(audio, AUDIO_FORMAT);
            tempFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

    public static String loadJSONFromAsset(Context c, String file) {
        String json;
        try {

            AssetManager mngr = c.getApplicationContext().getAssets();
            InputStream is = mngr.open(file);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
