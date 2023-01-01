package io.github.teccheck.fastlyrics.ui.lyrics

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

class LyricsViewModel : ViewModel() {

    private val _songMeta = MutableLiveData<SongMeta>()
    private val _songWithLyrics = MutableLiveData<SongWithLyrics>()
    private val _loading = MutableLiveData<Boolean>()

    val songMeta: LiveData<SongMeta> = _songMeta
    val songWithLyrics: LiveData<SongWithLyrics> = _songWithLyrics
    val loading: LiveData<Boolean> = _loading

    fun loadLyricsForCurrentSong(context: Context) {
        if (!DummyNotificationListenerService.canAccessNotifications(context)) {
            Log.w(TAG, "Can't access notifications")
            _loading.value = false
            return
        }
        _loading.value = true

        val songMeta = MediaSession.getSongInformation(context)
        songMeta?.let {
            _songMeta.value = it
        }

        // TODO: Fetch lyrics

        _loading.value = false
    }

    fun loadLyricsForSongWithId(id: Int) {
        // TODO
    }

    companion object {
        private const val TAG = "LyricsViewModel"
    }
}