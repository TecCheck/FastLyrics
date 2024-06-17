package io.github.teccheck.fastlyrics.api

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.NoMusicPlayingException
import io.github.teccheck.fastlyrics.exceptions.NoNotifPermsException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

object MediaSession {

    private const val TAG = "MediaSession"

    private lateinit var nls: ComponentName
    private lateinit var msm: MediaSessionManager

    private var activeMediaSession: MediaController? = null
    private val internalCallbacks = mutableMapOf<MediaSession.Token, MediaController.Callback>()
    private val callbacks = mutableListOf<SongMetaCallback>()

    private var initialized = false

    fun init(context: Context) {
        if (!DummyNotificationListenerService.canAccessNotifications(context))
            return

        if (initialized)
            return

        initialized = true

        nls = ComponentName(context, DummyNotificationListenerService::class.java)
        msm = context.getSystemService()!!
        msm.addOnActiveSessionsChangedListener(this::onActiveSessionsChanged, nls)

        val activeSessions = msm.getActiveSessions(nls)
        val activeSession = activeSessions.find { isActive(it.playbackState) }
        activeMediaSession = activeSession ?: activeSessions.firstOrNull()
        onActiveSessionsChanged(activeSessions)

        Log.d(TAG, "INIT: $nls, $msm")
    }

    private fun onActiveSessionsChanged(controllers: List<MediaController?>?) {
        val callbacks = mutableMapOf<MediaSession.Token, MediaController.Callback>()
        controllers?.filterNotNull()?.forEach {
            Log.d(TAG, "Session: $it (${it.sessionToken})")

            if (internalCallbacks.containsKey(it.sessionToken)) {
                callbacks[it.sessionToken] = internalCallbacks[it.sessionToken]!!
            } else {
                val callback = object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) =
                        this@MediaSession.onPlaybackStateChanged(it, state)

                    override fun onMetadataChanged(metadata: MediaMetadata?) =
                        this@MediaSession.onMetadataChanged(it, metadata)
                }

                it.registerCallback(callback)
                callbacks[it.sessionToken] = callback
            }
        }

        internalCallbacks.clear()
        internalCallbacks.putAll(callbacks)
    }

    private fun onPlaybackStateChanged(controller: MediaController, state: PlaybackState?) {
        if (isActive(state)) setActiveMediaSession(controller)
    }

    private fun onMetadataChanged(controller: MediaController, metadata: MediaMetadata?) {
        if (controller.sessionToken != activeMediaSession?.sessionToken) return
        val songMeta = metadata?.getSongMeta() ?: return
        callbacks.forEach { it.onSongMetaChanged(songMeta) }
    }

    private fun setActiveMediaSession(newActive: MediaController) {
        activeMediaSession = newActive
        onMetadataChanged(newActive, newActive.metadata)
    }

    fun getSongInformation(): Result<SongMeta, LyricsApiException> {
        if (!initialized) return Failure(NoNotifPermsException())

        val session = activeMediaSession ?: return Failure(NoMusicPlayingException())
        val metadata = session.metadata?.getSongMeta() ?: return Failure(
            NoMusicPlayingException()
        )

        return Success(metadata)
    }

    fun getSongPosition(): Long? = activeMediaSession?.playbackState?.position

    fun registerSongMetaCallback(callback: SongMetaCallback) {
        if (!initialized) return

        callbacks.add(callback)
    }

    fun unregisterSongMetaCallback(callback: SongMetaCallback) {
        if (!initialized) return

        callbacks.remove(callback)
    }

    private fun MediaMetadata.getSongMeta(): SongMeta? {
        // Some of those attributes may be stored with different keys (depending on the device)
        // For the ?: syntax see https://kotlinlang.org/docs/null-safety.html#elvis-operator
        val title = getString(MediaMetadata.METADATA_KEY_TITLE)
        val album = getString(MediaMetadata.METADATA_KEY_ALBUM)
        val artist = getString(MediaMetadata.METADATA_KEY_ARTIST) ?: getString(
            MediaMetadata.METADATA_KEY_ALBUM_ARTIST
        )
        val art = getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: getBitmap(
            MediaMetadata.METADATA_KEY_ART
        )

        if (title == null)
            return null

        return SongMeta(title, artist, album, art)
    }

    private fun isActive(playbackState: PlaybackState?): Boolean {
        if (playbackState == null)
            return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return playbackState.isActive

        return when (playbackState.state) {
            PlaybackState.STATE_FAST_FORWARDING, PlaybackState.STATE_REWINDING, PlaybackState.STATE_SKIPPING_TO_PREVIOUS, PlaybackState.STATE_SKIPPING_TO_NEXT, PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM, PlaybackState.STATE_BUFFERING, PlaybackState.STATE_CONNECTING, PlaybackState.STATE_PLAYING -> true
            else -> false
        }
    }

    fun interface SongMetaCallback {
        fun onSongMetaChanged(songMeta: SongMeta)
    }
}