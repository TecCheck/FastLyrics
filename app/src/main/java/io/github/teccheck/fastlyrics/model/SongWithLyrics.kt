package io.github.teccheck.fastlyrics.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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
    val duration: Long?,

    @Deprecated("Because there can be both synced and plain lyrics, this is not need.")
    @ColumnInfo(defaultValue = "RAW_TEXT")
    val type: LyricsType,

    @ColumnInfo(defaultValue = "genius")
    val provider: String
) : Serializable {
    override fun toString(): String {
        return "SongWithLyrics(id=$id, title='$title', artist='$artist', lyricsPlain=$lyricsPlain, lyricsSynced=$lyricsSynced, sourceUrl='$sourceUrl', album=$album, artUrl=$artUrl, type=$type, provider='$provider')"
    }
}