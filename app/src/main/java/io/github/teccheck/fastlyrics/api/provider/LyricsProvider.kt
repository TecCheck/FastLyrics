package io.github.teccheck.fastlyrics.api.provider

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics

interface LyricsProvider {
    fun getName(): String

    fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException>

    fun search(songMeta: SongMeta): Result<List<SearchResult>, LyricsApiException> {
        var searchQuery = songMeta.title
        if (songMeta.artist != null) {
            searchQuery += " ${songMeta.artist}"
        }

        return search(searchQuery)
    }

    fun fetchLyrics(songId: Int): Result<SongWithLyrics, LyricsApiException>

    fun fetchLyrics(searchResult: SearchResult): Result<SongWithLyrics, LyricsApiException> {
        if (searchResult.id == null) return Failure(LyricsNotFoundException())

        return fetchLyrics(searchResult.id)
    }

    companion object {
        fun getAllProviders(): Array<LyricsProvider> {
            return arrayOf(Deezer, Genius, LrcLib)
        }

        fun getProviderByName(name: String) = getAllProviders().find { it.getName() == name }
    }
}