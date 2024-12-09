package io.github.teccheck.fastlyrics.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.utils.ProviderOrder
import java.util.concurrent.Executors

object LyricsApi {
    private const val TAG = "LyricsApi"

    private val executor = Executors.newFixedThreadPool(2)

    private val providers: Array<LyricsProvider> get() = ProviderOrder.providers
    private val defaultProvider: LyricsProvider get() = providers.first()

    fun getLyricsAsync(
        songMeta: SongMeta,
        liveDataTarget: MutableLiveData<Result<SongWithLyrics, LyricsApiException>>,
        synced: Boolean = false
    ) {
        executor.submit {
            Log.d(TAG, "getLyricsAsync($songMeta, $synced)")

            val type = if (synced) LyricsType.LRC else LyricsType.RAW_TEXT
            val song = songMeta.artist?.let { LyricStorage.findSong(songMeta.title, it, type) }
            if (song != null) {
                Log.d(TAG, "Found cached: $song")
                liveDataTarget.postValue(Success(song))
                return@submit
            }

            val result = fetchLyrics(songMeta, synced)
            liveDataTarget.postValue(result)

            if (result is Success) {
                LyricStorage.store(result.value)
            }
        }
    }

    fun getLyricsAsync(
        searchResult: SearchResult,
        liveDataTarget: MutableLiveData<Result<SongWithLyrics, LyricsApiException>>
    ) {
        executor.submit {
            val result = fetchLyrics(searchResult)
            liveDataTarget.postValue(result)

            if (result is Success) {
                LyricStorage.store(result.value)
            }
        }
    }

    fun search(
        query: String,
        liveDataTarget: MutableLiveData<Result<List<SearchResult>, LyricsApiException>>,
        provider: LyricsProvider = this.defaultProvider
    ) {
        executor.submit { liveDataTarget.postValue(provider.search(query)) }
    }

    private fun fetchLyrics(
        songMeta: SongMeta, synced: Boolean = false
    ): Result<SongWithLyrics, LyricsApiException> {
        Log.d(TAG, "fetchLyrics($songMeta, $synced)")
        var bestResult: SearchResult? = null
        var bestResultScore = 0.0

        for (provider in providers) {
            val search = provider.search(songMeta)
            if (search !is Success) continue

            val result = search.value.maxByOrNull { getResultScore(songMeta, it) } ?: continue
            val score = getResultScore(songMeta, result)

            if (score > bestResultScore) {
                bestResult = result
                bestResultScore = score
            }
        }

        return fetchLyrics(bestResult)
    }

    private fun fetchLyrics(searchResult: SearchResult?): Result<SongWithLyrics, LyricsApiException> {
        searchResult?.songWithLyrics?.let {
            Log.d(TAG, "Can skip fetch because song is present in search result.")
            return Success(it)
        }

        if (searchResult?.id == null) return Failure(LyricsNotFoundException())
        return searchResult.provider.fetchLyrics(searchResult)
    }

    private fun getResultScore(songMeta: SongMeta, searchResult: SearchResult): Double {
        var score = 0.0

        if (songMeta.title == searchResult.title) score += 0.5
        else if (songMeta.title.startsWith(searchResult.title)) score += 0.4
        else if (searchResult.title.startsWith(songMeta.title)) score += 0.3

        if (songMeta.artist == null) return score
        else if (songMeta.artist == searchResult.artist) score += 0.5
        else if (songMeta.artist.startsWith(searchResult.artist)) score += 0.4
        else if (searchResult.artist.startsWith(songMeta.artist)) score += 0.3

        if (songMeta.album == searchResult.album) score += 0.5

        return score
    }
}