package com.michaeljordanr.memesounds

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset


object Utils {
    const val AUDIO_FORMAT = ".mp3"
    const val APP_CENTER_KEY = "6e27bac1-b9c7-48c1-aec1-8a08c6512fca"
    const val FLURRY_KEY = "CC6FRJVJF7KS7FSM2RDP"

    fun getDrawableId(c: Context, imageName: String): Int {
        val packageName = c.applicationContext.packageName
        return c.resources.getIdentifier(imageName, "drawable", packageName)
    }

    fun getFileAudio(c: Context, audio: String): File? {
        try {
            val id: Int = c.resources.getIdentifier(audio.removeSuffix(AUDIO_FORMAT), "raw", c.packageName)
            val `in`: InputStream = c.resources.openRawResource(id)
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

    fun shareAudio(context: Context, audio: Audio) {
        try {
            val audioName = audio.audioName.removeSuffix(AUDIO_FORMAT)
            val tmpFile = File(context.cacheDir.toString() + "/$audioName + $AUDIO_FORMAT")
            val id: Int = context.resources.getIdentifier(audioName.removeSuffix(AUDIO_FORMAT), "raw", context.packageName)
            val `in`: InputStream = context.resources.openRawResource(id)
            val out = FileOutputStream(tmpFile, false)
            val buff = ByteArray(1024)
            var read: Int
            try {
                while (`in`.read(buff).also { read = it } > 0) {
                    out.write(buff, 0, read)
                }
            } finally {
                `in`.close()
                out.close()
            }
            val uri: Uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", tmpFile.absoluteFile)
            val share = Intent(Intent.ACTION_SEND)
            share.type = "audio/mpeg3"
            share.putExtra(Intent.EXTRA_STREAM, uri)
            val intent = Intent.createChooser(share, audio.audioDescription)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
        }
    }
}
