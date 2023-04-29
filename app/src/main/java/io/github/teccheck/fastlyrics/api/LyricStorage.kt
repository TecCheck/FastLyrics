package io.github.teccheck.fastlyrics.api

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import io.github.teccheck.fastlyrics.api.storage.LyricsDatabase
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.utils.Utils
import java.util.concurrent.Executors
import dev.forkhandles.result4k.Result

object LyricStorage {
    private const val TAG = "LyricsStorage"

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var database: LyricsDatabase

    fun init(context: Context) {
        database = Room.databaseBuilder(context, LyricsDatabase::class.java, "lyrics").build()
    }

    fun deleteAsync(song: SongWithLyrics) {
        executor.submit { database.songsDao().delete(song) }
    }

    fun fetchSongsAsync(liveDataTarget: MutableLiveData<Result<List<SongWithLyrics>, LyricsApiException>>) {
        Log.d(TAG, "fetchSongsAsync")
        executor.submit {
            liveDataTarget.postValue(
                Utils.result(
                    database.songsDao().getAll(),
                    LyricsNotFoundException()
                )
            )
        }
    }

    fun findLyricsAsync(
        title: String,
        artist: String,
        liveDataTarget: MutableLiveData<Result<SongWithLyrics, LyricsApiException>>
    ) {
        Log.d(TAG, "findLyricsAsync")
        executor.submit {
            liveDataTarget.postValue(
                Utils.result(
                    findSong(title, artist), LyricsNotFoundException()
                )
            )
        }
    }

    fun store(song: SongWithLyrics) {
        Log.d(TAG, "store")
        database.songsDao().insert(song)
    }

    fun findSong(title: String, artist: String): SongWithLyrics? =
        database.songsDao().findSong(title, artist)
}