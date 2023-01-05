package io.github.teccheck.fastlyrics.ui.lyrics

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.api.LyricsApi
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

class LyricsViewModel : ViewModel() {

    private val _songMeta = MutableLiveData<SongMeta>()
    private val _songWithLyrics = MutableLiveData<SongWithLyrics>()

    val songMeta: LiveData<SongMeta> = _songMeta
    val songWithLyrics: LiveData<SongWithLyrics?> = _songWithLyrics

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
        _songWithLyrics.value =
            LyricStorage.getLyrics().find { it.artist == artist && it.title == title }
    }

    companion object {
        private const val TAG = "LyricsViewModel"
    }
}