package io.github.teccheck.fastlyrics.api

import androidx.lifecycle.MutableLiveData
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import java.util.concurrent.Executors

object LyricsApi {

    private const val TAG = "LyricsApi"

    private val executor = Executors.newSingleThreadExecutor()

    fun fetchLyrics(songMeta: SongMeta, liveDataTarget: MutableLiveData<SongWithLyrics>) {
        executor.submit {
            var searchQuery = songMeta.title
            if (songMeta.artist != null) {
                searchQuery += " ${songMeta.artist}"
            }

            var song: SongWithLyrics? = null
            val result = Genius.search(searchQuery)
            if (result != null && result.isNotEmpty()) {
                song = result[0].id?.let { Genius.fetchLyrics(it) }
            }

            liveDataTarget.postValue(song)
            song?.let {
                LyricStorage.store(it, true)
            }
        }
    }
}