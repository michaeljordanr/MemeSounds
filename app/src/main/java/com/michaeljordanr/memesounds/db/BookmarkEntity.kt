package com.michaeljordanr.memesounds.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class BookmarkEntity(
    @PrimaryKey val id: Int? = null,
    val audioName: String
)