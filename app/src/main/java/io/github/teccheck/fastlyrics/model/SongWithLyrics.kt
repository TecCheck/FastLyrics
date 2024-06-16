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
    val lyricsPlain: String?,
    val lyricsSynced: String?,
    val sourceUrl: String,
    val album: String?,
    val artUrl: String?,

    @ColumnInfo(defaultValue = "RAW_TEXT")
    val type: LyricsType,

    @ColumnInfo(defaultValue = "genius")
    val provider: String
) {
    fun getDefaultLyrics() = lyricsPlain ?: lyricsSynced ?: ""

    override fun toString(): String {
        return "SongWithLyrics(id=$id, title='$title', artist='$artist', lyricsPlain=$lyricsPlain, lyricsSynced=$lyricsSynced, sourceUrl='$sourceUrl', album=$album, artUrl=$artUrl, type=$type, provider='$provider')"
    }
}