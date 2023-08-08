package com.michaeljordanr.memesounds.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookmarkEntity::class],
    version = 1
)
abstract class BookmarkDatabase: RoomDatabase() {

    abstract val dao: BookmarkDao
}