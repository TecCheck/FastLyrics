package io.github.teccheck.fastlyrics.api.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.github.teccheck.fastlyrics.model.SongWithLyrics

@Dao
interface SongsDao {

    @Query("SELECT * FROM songs")
    fun getAll(): List<SongWithLyrics>

    @Query("SELECT * FROM songs WHERE title = :title AND artist = :artist")
    fun findSong(title: String, artist: String): SongWithLyrics?

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSong(id: Long): SongWithLyrics?

    @Insert
    fun insert(song: SongWithLyrics)

    @Delete
    fun delete(song: SongWithLyrics)
}