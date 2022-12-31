package io.github.teccheck.fastlyrics.api

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.appcompat.app.AppCompatActivity
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

object MediaSession {

    fun getSongInformation(context: Context): SongMeta? {
        val className = ComponentName(context, DummyNotificationListenerService::class.java)
        val mediaSessionManager = context.getSystemService(AppCompatActivity.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val controllers: List<MediaController> = mediaSessionManager.getActiveSessions(className)

        if (controllers.isEmpty()) return null
        val metadata = controllers[0].metadata ?: return null

        // Some of those attributes may be stored with different keys (depending on the device)
        // For the ?: syntax see https://kotlinlang.org/docs/null-safety.html#elvis-operator
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        val art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)

        return SongMeta(title, artist, album, art)
    }
}