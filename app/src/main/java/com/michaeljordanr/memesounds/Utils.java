package com.michaeljordanr.memesounds;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static int getDrawableId(Context c, String imageName) {
        String packageName = c.getApplicationContext().getPackageName();
        return c.getResources().getIdentifier(imageName, "drawable", packageName);
    }

    public static int getRawId(Context c, String audio){
        String packageName = c.getApplicationContext().getPackageName();
        return c.getResources().getIdentifier(audio, "raw", packageName);
    }

    public static String loadJSONFromAsset(Context c, String file) {
        String json = null;
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
