package com.michaeljordanr.memesounds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog.*

class BottomSheetFragment(private val listener: BottomSheetFragmentListener) : BottomSheetDialogFragment() {
    private lateinit var audio: Audio
    private var type = AudioListFragment.AudioListType.ALL

    private var customApplication: CustomApplication? = null

    interface BottomSheetFragmentListener {
        fun onFinished()
        fun unbookmark(audio: Audio)
        fun bookmark(audio: Audio)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            customApplication = activity?.applicationContext as? CustomApplication
        }

        if (type == AudioListFragment.AudioListType.FAVORITES) {
            bt_favorite.text = getString(R.string.unbookmark)
        }

        bt_share.setOnClickListener {
            shareAudio(audio)
            listener.onFinished()
        }

        bt_favorite.setOnClickListener {
            if (type == AudioListFragment.AudioListType.FAVORITES) {
                listener.unbookmark(audio)
            } else {
                listener.bookmark(audio)
            }

            listener.onFinished()
        }
    }

    fun setData(audio: Audio, typeList: AudioListFragment.AudioListType) {
        this.audio = audio
        type = typeList
    }

    private fun shareAudio(audio: Audio) {
        context?.let {
            Utils.shareAudio(it, audio)

            customApplication?.let { app ->
                val params = HashMap<String, String>()
                params["audio_name"] = audio.audioName

                app.sendAnalytics("share_audio", params)
            }
        }
    }
}