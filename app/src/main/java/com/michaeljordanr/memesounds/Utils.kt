package com.michaeljordanr.memesounds

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.michaeljordanr.memesounds.model.Audio
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

object Utils {
    const val AUDIO_FORMAT = ".mp3"

    fun getDrawableId(c: Context, imageName: String): Int {
        val packageName = c.applicationContext.packageName
        return c.resources.getIdentifier(imageName, "drawable", packageName)
    }

    fun getFileAudio(c: Context, audio: String): File? {
        try {
            val id = getAudioIdFromRaw(c, audio)
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
            val tmpFile = File(context.cacheDir.toString() + "/$audioName$AUDIO_FORMAT")
            val id = getAudioIdFromRaw(context, audioName)
            val bytes = context.resources.openRawResource(id).use {
                    it.readBytes()
            }
            FileOutputStream(tmpFile).use {
                it.write(bytes)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tmpFile.absoluteFile
            )

            val share = Intent(Intent.ACTION_SEND)
            share.type = "audio/mpeg3"
            share.putExtra(Intent.EXTRA_STREAM, uri)
            val intent = Intent.createChooser(share, audio.audioDescription)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun getAudioIdFromRaw(context: Context, audioName: String): Int {
        return context.resources.getIdentifier(
            audioName.removeSuffix(AUDIO_FORMAT),
            "raw",
            context.packageName
        )
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        view?.let {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}
