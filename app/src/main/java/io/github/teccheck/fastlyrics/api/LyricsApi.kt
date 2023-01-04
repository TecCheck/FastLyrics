package io.github.teccheck.fastlyrics.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.provider.LyricStorage
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
            Log.d(TAG, "Search result: $result")

            if (result != null && result.isNotEmpty()) {
                song = result[0].id?.let { Genius.fetchLyrics(it) }
            }

            Log.d(TAG, "Found: $song")

            song?.let { LyricStorage.store(it) }
            liveDataTarget.postValue(song)
        }
    }
}