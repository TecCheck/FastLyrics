package io.github.teccheck.fastlyrics.api

import androidx.lifecycle.MutableLiveData
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.api.provider.Deezer
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import java.util.concurrent.Executors

object LyricsApi {

    private const val TAG = "LyricsApi"

    private val executor = Executors.newSingleThreadExecutor()

    private val providers: Array<LyricsProvider> = arrayOf(Genius, Deezer)
    private val provider: LyricsProvider = providers.first()

    fun getLyricsAsync(
        songMeta: SongMeta,
        liveDataTarget: MutableLiveData<Result<SongWithLyrics, LyricsApiException>>
    ) {
        executor.submit {
            val song = songMeta.artist?.let { LyricStorage.findSong(songMeta.title, it) }
            if (song != null) {
                liveDataTarget.postValue(Success(song))
                return@submit
            }

            val result = fetchLyrics(songMeta)
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
            val result = searchResult.id?.let { provider.fetchLyrics(it) } ?: Failure(
                LyricsNotFoundException()
            )
            liveDataTarget.postValue(result)

            if (result is Success) {
                LyricStorage.store(result.value)
            }
        }
    }

    fun search(
        query: String,
        liveDataTarget: MutableLiveData<Result<List<SearchResult>, LyricsApiException>>
    ) {
        executor.submit {
            liveDataTarget.postValue(provider.search(query))
            val results = provider.search(query)

            if (results is Success)
                results.value[0].id?.let { provider.fetchLyrics(it) }
        }
    }

    private fun fetchLyrics(
        songMeta: SongMeta
    ): Result<SongWithLyrics, LyricsApiException> {
        var searchQuery = songMeta.title
        if (songMeta.artist != null) {
            searchQuery += " ${songMeta.artist}"
        }
        return when (val searchResult = provider.search(searchQuery)) {
            is Failure -> {
                searchResult
            }

            is Success -> {
                if (searchResult.value.isNotEmpty()) {
                    provider.fetchLyrics(searchResult.value[0].id!!)
                } else {
                    Failure(LyricsNotFoundException())
                }
            }
        }
    }
}