package com.michaeljordanr.memesounds

import com.google.gson.annotations.SerializedName

data class Audio(
        var id: Int,
        @SerializedName("audio_name")
        var audioName: String,
        @SerializedName("audio_description")
        var audioDescription: String,
        @SerializedName("audio_thumb")
        var audioThumb: String
)
