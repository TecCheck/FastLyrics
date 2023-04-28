package io.github.teccheck.fastlyrics.ui.lyrics

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.forkhandles.result4k.Failure
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.api.LyricsApi
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException

class LyricsViewModel : ViewModel() {

    private val _songMeta = MutableLiveData<Result<SongMeta, LyricsApiException>>()
    private val _songWithLyrics = MutableLiveData<Result<SongWithLyrics, LyricsApiException>>()

    val songMeta: LiveData<Result<SongMeta, LyricsApiException>> = _songMeta
    val songWithLyrics: LiveData<Result<SongWithLyrics, LyricsApiException>> = _songWithLyrics

    fun loadLyricsForCurrentSong(context: Context): Boolean {
        if (!DummyNotificationListenerService.canAccessNotifications(context)) {
            Log.w(TAG, "Can't access notifications")
            return false
        }

        val songMetaResult = MediaSession.getSongInformation(context)
        _songMeta.value = songMetaResult

        when(songMetaResult) {
            is Success -> {
                val songMeta = songMetaResult.value
                val lyrics = LyricStorage.findLyrics(songMeta.title, songMeta.artist ?: "")
                if (lyrics != null) {
                    _songWithLyrics.value = Success(lyrics)
                } else {
                    LyricsApi.fetchLyrics(songMeta, _songWithLyrics)
                }
            }

            else -> {}
        }

        return songMetaResult is Success
    }

    fun loadLyricsForSongFromStorage(title: String, artist: String) {
        val lyrics = LyricStorage.findLyrics(title, artist)
        _songWithLyrics.value = if (lyrics != null) {
            Success(lyrics)
        } else {
            Failure(LyricsNotFoundException())
        }
    }

    companion object {
        private const val TAG = "LyricsViewModel"
    }
}