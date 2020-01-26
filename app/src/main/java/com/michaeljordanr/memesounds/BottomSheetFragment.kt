package com.michaeljordanr.memesounds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flurry.android.FlurryAgent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.microsoft.appcenter.analytics.Analytics
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog.*

class BottomSheetFragment(private val listener: BottomSheetFragmentListener) : BottomSheetDialogFragment() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var audio: Audio
    private var type = AudioListFragment.AudioListType.ALL

    interface BottomSheetFragmentListener {
        fun onFinished()
        fun unbookmark(id: Int)
        fun bookmark(audio: Audio)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            firebaseAnalytics = FirebaseAnalytics.getInstance(it)
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
                listener.unbookmark(audio.id)
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

            val audioName = audio.audioName
            val params = Bundle()
            val shareAudioLog = "share_audio"
            params.putString("audio_name", audioName)
            firebaseAnalytics.logEvent(shareAudioLog, params)

            val logParams = HashMap<String, String>()
            logParams["audio_name"] = audioName
            FlurryAgent.logEvent(shareAudioLog, logParams)
            Analytics.trackEvent(shareAudioLog, logParams)
        }
    }
}