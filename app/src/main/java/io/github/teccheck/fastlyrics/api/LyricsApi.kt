package io.github.teccheck.fastlyrics.api

import androidx.lifecycle.MutableLiveData
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.provider.LyricStorage
import java.util.concurrent.Executors

object LyricsApi {

    private const val TAG = "LyricsApi"

    private val executor = Executors.newSingleThreadScheduledExecutor()

    fun fetchLyrics(songMeta: SongMeta, liveDataTarget: MutableLiveData<SongWithLyrics>) {
        executor.submit {
            // TODO: Implement actual fetching
            val song = SongWithLyrics(
                "Some Title",
                "Some Artist",
                "Lyrics will go here.",
                "https://example.com",
                null,
                null
            )

            LyricStorage.store(song)

            liveDataTarget.postValue(song)
        }
    }
}