package io.github.teccheck.fastlyrics.api

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.appcompat.app.AppCompatActivity
import dev.forkhandles.result4k.Failure
import io.github.teccheck.fastlyrics.exceptions.NoMusicPlayingException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

object MediaSession {

    fun getSongInformation(context: Context): Result<SongMeta, NoMusicPlayingException> {
        return when (val result = getDefaultMediaController(context)) {
            is Failure -> result
            is Success -> {
                val metadata = result.value.metadata ?: return Failure(NoMusicPlayingException())
                Success(metadata.getSongMeta())
            }
        }
    }

    fun registerSongMetaCallback(context: Context, callback: SongMetaCallback) {
        val result = getDefaultMediaController(context)
        if (result is Success) result.value.registerCallback(callback)
    }

    fun unregisterSongMetaCallback(context: Context, callback: SongMetaCallback) {
        getMediaControllers(context).forEach { it.unregisterCallback(callback) }
    }

    private fun getDefaultMediaController(context: Context): Result<MediaController, NoMusicPlayingException> {
        val controllers = getMediaControllers(context)
        if (controllers.isEmpty()) return Failure(NoMusicPlayingException())
        return Success(controllers[0])
    }

    private fun getMediaControllers(context: Context): List<MediaController> {
        val className = ComponentName(context, DummyNotificationListenerService::class.java)
        val mediaSessionManager =
            context.getSystemService(AppCompatActivity.MEDIA_SESSION_SERVICE) as MediaSessionManager
        return mediaSessionManager.getActiveSessions(className)
    }

    private fun MediaMetadata.getSongMeta(): SongMeta {
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

        return SongMeta(title, artist, album, art)
    }

    abstract class SongMetaCallback : MediaController.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            if (metadata != null) onSongMetaChanged(metadata.getSongMeta())
        }

        abstract fun onSongMetaChanged(songMeta: SongMeta)
    }
}