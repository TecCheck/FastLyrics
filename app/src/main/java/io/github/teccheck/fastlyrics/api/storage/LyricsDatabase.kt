package io.github.teccheck.fastlyrics.api.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.teccheck.fastlyrics.model.SongWithLyrics

@Database(entities = [SongWithLyrics::class], version = 1)
abstract class LyricsDatabase : RoomDatabase() {
    abstract fun songsDao(): SongsDao
}