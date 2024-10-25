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
}