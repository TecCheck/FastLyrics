package io.github.teccheck.fastlyrics.model

import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import java.io.Serializable

data class SearchResult(
    val title: String,
    val artist: String,
    val album: String?,
    val artUrl: String?,
    val url: String?,
    val id: Long?,
    val provider: LyricsProvider,
    val songWithLyrics: SongWithLyrics? = null
) : Serializable {
    override fun toString(): String {
        return "SearchResult(title='$title', artist='$artist', album=$album, artUrl=$artUrl, url=$url, id=$id, provider=$provider, songWithLyrics=$songWithLyrics)"
    }
}
