package io.github.teccheck.fastlyrics.api.provider

import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics

interface LyricsProvider {
    fun getName(): String

    fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException>

    fun search(songMeta: SongMeta): Result<List<SearchResult>, LyricsApiException> {
        return search("${songMeta.title} ${songMeta.artist ?: ""}")
    }

    fun fetchLyrics(searchResult: SearchResult): Result<SongWithLyrics, LyricsApiException>

    companion object {
        fun getAllProviders(): Array<LyricsProvider> {
            return arrayOf(PetitLyrics, Deezer, Genius)
        }

        fun getProviderByName(name: String) = getAllProviders().find { it.getName() == name }
    }
}