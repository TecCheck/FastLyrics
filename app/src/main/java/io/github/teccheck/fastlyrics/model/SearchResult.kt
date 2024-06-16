package io.github.teccheck.fastlyrics.model

import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import java.io.Serializable

data class SearchResult(
    val title: String,
    val artist: String,
    val album: String?,
    val artUrl: String?,
    val url: String?,
    val id: Int?,
    val provider: LyricsProvider,
    val songWithLyrics: SongWithLyrics? = null
) : Serializable
