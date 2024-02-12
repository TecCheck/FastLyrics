package io.github.teccheck.fastlyrics.ui.fastlyrics

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.api.LyricsApi
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.log

class FastLyricsViewModel : ViewModel() {

    private val _songMeta = MutableLiveData<Result<SongMeta, LyricsApiException>>()
    private val _songWithLyrics = MutableLiveData<Result<SongWithLyrics, LyricsApiException>>()
    private val _songWithLyricsSynced = MutableLiveData<Result<SongWithLyrics, LyricsApiException>>()
    private val _songPosition = MutableLiveData<Long>()
     var storeLyric=""

    private var songPositionTimer: Timer? = null

    val songMeta: LiveData<Result<SongMeta, LyricsApiException>> = _songMeta
    val songWithLyrics: LiveData<Result<SongWithLyrics, LyricsApiException>> = _songWithLyrics
    val songWithLyricsSynced: LiveData<Result<SongWithLyrics, LyricsApiException>> = _songWithLyricsSynced
    val songPosition: LiveData<Long> = _songPosition

    var syncedLyricsAvailable = false
    var plainLyricsAvailable = false
    val bothLyricsAvailable
        get() = syncedLyricsAvailable && plainLyricsAvailable

    var autoRefresh = false

    private val songMetaCallback = MediaSession.SongMetaCallback {
        if (!autoRefresh) return@SongMetaCallback

        _songMeta.postValue(Success(it))
        loadLyrics(it)
    }

    fun loadLyricsForCurrentSong(context: Context): Boolean {
        if (!DummyNotificationListenerService.canAccessNotifications(context)) {
            Log.w(TAG, "Can't access notifications")
            return false
        }

        val songMetaResult = MediaSession.getSongInformation()
        _songMeta.value = songMetaResult

        if (songMetaResult is Success) {
            loadLyrics(songMetaResult.value)
            storeLyric=songMetaResult.value.toString()

            Log.d(TAG, "Stored lyric: $storeLyric")
        }

        return songMetaResult is Success
    }

    private fun loadLyrics(songMeta: SongMeta) {
        syncedLyricsAvailable = false
        plainLyricsAvailable = false
        LyricsApi.getLyricsAsync(songMeta, _songWithLyrics, false)
        LyricsApi.getLyricsAsync(songMeta, _songWithLyricsSynced, true)
    }

    fun setupSongMetaListener() {
        MediaSession.registerSongMetaCallback(songMetaCallback)
    }

    fun setupPositionPolling(enabled: Boolean) {
        if (enabled) {
            val timer = Timer()
            timer.scheduleAtFixedRate(REFRESH_DELAY, REFRESH_DELAY) {
                _songPosition.postValue(MediaSession.getSongPosition())
            }

            songPositionTimer = timer
        } else {
            songPositionTimer?.cancel()
            songPositionTimer = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaSession.unregisterSongMetaCallback(songMetaCallback)
    }

    companion object {
        private const val TAG = "FastLyricsViewModel"
        private const val REFRESH_DELAY = 500L
    }
}