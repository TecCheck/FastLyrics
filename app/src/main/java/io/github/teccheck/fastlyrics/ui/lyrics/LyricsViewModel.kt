package io.github.teccheck.fastlyrics.ui.lyrics

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.api.LyricsApi
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

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

        if (songMetaResult is Success) {
            LyricsApi.getLyrics(songMetaResult.value, _songWithLyrics)
        }

        return songMetaResult is Success
    }

    fun loadLyricsForSongFromStorage(songId: Long) {
        LyricStorage.getSongAsync(songId, _songWithLyrics)
    }

    companion object {
        private const val TAG = "LyricsViewModel"
    }
}