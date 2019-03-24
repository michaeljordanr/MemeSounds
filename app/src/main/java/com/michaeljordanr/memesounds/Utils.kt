package com.michaeljordanr.memesounds

import android.content.Context
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset

object Utils {
    const val AUDIO_FORMAT = ".mp3"

    fun getDrawableId(c: Context, imageName: String): Int {
        val packageName = c.applicationContext.packageName
        return c.resources.getIdentifier(imageName, "drawable", packageName)
    }

    fun getFileAudio(c: Context, audio: String): File? {
        try {
            val am = c.assets
            val `in` = am.open(audio + AUDIO_FORMAT)
            val tempFile = File.createTempFile(audio, AUDIO_FORMAT)
            tempFile.deleteOnExit()
            val out = FileOutputStream(tempFile)
            IOUtils.copy(`in`, out)

            return tempFile
        } catch (e: IOException) {
            e.printStackTrace()

        }

        return null
    }

    fun loadJSONFromAsset(c: Context, file: String): String? {
        val json: String
        try {

            val mngr = c.applicationContext.assets
            val `is` = mngr.open(file)

            val size = `is`.available()

            val buffer = ByteArray(size)

            `is`.read(buffer)

            `is`.close()

            json = String(buffer, Charset.forName("UTF-8"))


        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json

    }
}
