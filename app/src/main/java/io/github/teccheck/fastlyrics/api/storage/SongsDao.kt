package io.github.teccheck.fastlyrics.api.storage

import androidx.room.Dao
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

    @Query("DELETE FROM songs WHERE id in (:ids)")
    fun deleteAll(ids: List<Long>)
}