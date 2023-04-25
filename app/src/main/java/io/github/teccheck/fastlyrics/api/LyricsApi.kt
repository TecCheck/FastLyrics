package io.github.teccheck.fastlyrics.api

import androidx.lifecycle.MutableLiveData
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import java.lang.Exception
import java.util.concurrent.Executors

object LyricsApi {

    private const val TAG = "LyricsApi"

    private val executor = Executors.newSingleThreadExecutor()

    fun fetchLyrics(songMeta: SongMeta, liveDataTarget: MutableLiveData<Result<SongWithLyrics>>) {
        executor.submit {
            var searchQuery = songMeta.title
            if (songMeta.artist != null) {
                searchQuery += " ${songMeta.artist}"
            }

            val searchResult = Genius.search(searchQuery)
            var songResult = Result.failure<SongWithLyrics>(Exception("Song not found"))
            searchResult.onSuccess {
                if (it.isNotEmpty()) {
                    songResult = Genius.fetchLyrics(it[0].id!!)
                }
            }

            liveDataTarget.postValue(songResult)

            songResult.onSuccess {
                LyricStorage.store(it, true)
            }
        }
    }
}