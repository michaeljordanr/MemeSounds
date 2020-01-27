package com.michaeljordanr.memesounds

import com.squareup.moshi.Json

data class Audio(
        @field:Json(name = "audio_id")
        var id: Int,
        @field:Json(name = "audio_name")
        var audioName: String,
        @field:Json(name = "audio_description")
        var audioDescription: String,
        @field:Json(name = "audio_thumb")
        var audioThumb: String
)
