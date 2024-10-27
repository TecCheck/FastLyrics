package io.github.teccheck.fastlyrics.model

import android.graphics.Bitmap

data class SongMeta(
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val art: Bitmap? = null,
    val duration: Long? = null,
) {
    override fun toString(): String {
        return "SongMeta(title='$title', artist=$artist, album=$album, art=$art, duration=$duration)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongMeta

        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        // Art is left out
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (art?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        return result
    }
}