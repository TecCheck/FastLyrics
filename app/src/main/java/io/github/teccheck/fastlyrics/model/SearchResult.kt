package io.github.teccheck.fastlyrics.model

data class SearchResult(
    val title: String,
    val artist: String,
    val album: String?,
    val url: String?,
    val id: Int?,
)
