package io.github.teccheck.fastlyrics.api

import androidx.lifecycle.MutableLiveData
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import java.util.concurrent.Executors

object LyricsApi {

    private const val TAG = "LyricsApi"

    private val executor = Executors.newSingleThreadExecutor()

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

    private fun fetchLyrics(
        songMeta: SongMeta
    ): Result<SongWithLyrics, LyricsApiException> {
        var searchQuery = songMeta.title
        if (songMeta.artist != null) {
            searchQuery += " ${songMeta.artist}"
        }

        return when (val searchResult = Genius.search(searchQuery)) {
            is Failure -> {
                searchResult
            }

            is Success -> {
                if (searchResult.value.isNotEmpty()) {
                    Genius.fetchLyrics(searchResult.value[0].id!!)
                } else {
                    Failure(LyricsNotFoundException())
                }
            }
        }
    }
}