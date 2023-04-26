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

    private val _songMeta = MutableLiveData<SongMeta>()
    private val _songWithLyrics = MutableLiveData<Result<SongWithLyrics, LyricsApiException>>()

    val songMeta: LiveData<SongMeta> = _songMeta
    val songWithLyrics: LiveData<Result<SongWithLyrics, LyricsApiException>> = _songWithLyrics

    fun loadLyricsForCurrentSong(context: Context): Boolean {
        if (!DummyNotificationListenerService.canAccessNotifications(context)) {
            Log.w(TAG, "Can't access notifications")
            return false
        }

        val songMeta = MediaSession.getSongInformation(context)
        songMeta?.let {
            _songMeta.value = it
            LyricsApi.fetchLyrics(it, _songWithLyrics)
        }

        return songMeta != null
    }

    fun loadLyricsForSongFromStorage(title: String, artist: String) {
        val lyrics = LyricStorage.getLyrics().find { it.artist == artist && it.title == title }

        if (lyrics != null) {
            _songWithLyrics.value = Success(lyrics)
        } else {
            _songWithLyrics.value = Failure(LyricsNotFoundException())
        }
    }

    companion object {
        private const val TAG = "LyricsViewModel"
    }
}