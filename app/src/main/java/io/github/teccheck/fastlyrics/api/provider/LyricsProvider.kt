package io.github.teccheck.fastlyrics.api.provider

import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics

interface LyricsProvider {
    fun getName(): String

    fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException>

    fun fetchLyrics(songId: Int): Result<SongWithLyrics, LyricsApiException>

    companion object {
        fun getAllProviders(): Array<LyricsProvider> {
            return arrayOf(Deezer, Genius)
        }
    }
}