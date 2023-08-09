package com.michaeljordanr.memesounds

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.michaeljordanr.memesounds.model.Audio
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.text.Normalizer

object Util {

    const val AUDIO_FORMAT = ".mp3"

    private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    fun CharSequence.unaccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return REGEX_UNACCENT.replace(temp, "")
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
}