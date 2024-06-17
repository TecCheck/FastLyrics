package io.github.teccheck.fastlyrics.model

import android.graphics.Bitmap

data class SongMeta(
    val title: String,
    val artist: String?,
    val album: String?,
    val art: Bitmap?
) {
    override fun toString(): String {
        return "SongMeta(title='$title', artist=$artist, album=$album, art=$art)"
    }
}