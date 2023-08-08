package com.michaeljordanr.memesounds.di

import android.app.Application
import androidx.room.Room
import com.michaeljordanr.memesounds.db.BookmarkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideBookmarkDatabase(app: Application): BookmarkDatabase {
        return Room.databaseBuilder(
            app, BookmarkDatabase::class.java, "bookmark_db"
        )
            .build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(db: BookmarkDatabase) = db.dao
}