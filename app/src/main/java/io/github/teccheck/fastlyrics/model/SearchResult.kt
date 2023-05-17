package io.github.teccheck.fastlyrics.model

import java.io.Serializable

data class SearchResult(
    val title: String,
    val artist: String,
    val album: String?,
    val artUrl: String?,
    val url: String?,
    val id: Int?,
) : Serializable
