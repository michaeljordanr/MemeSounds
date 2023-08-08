package com.michaeljordanr.memesounds.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarkentity WHERE id = :bookmarkId")
    suspend fun deleteBookmark(bookmarkId: Int)

    @Query("SELECT * FROM bookmarkentity")
    suspend fun getBookmarks(): List<BookmarkEntity>
}