package io.github.teccheck.fastlyrics.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongWithLyrics(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val artist: String,
    val lyrics: String,
    val sourceUrl: String,
    val album: String?,
    val artUrl: String?,

    @ColumnInfo(defaultValue = "RAW_TEXT")
    val type: LyricsType,

    @ColumnInfo(defaultValue = "genius")
    val provider: String
)