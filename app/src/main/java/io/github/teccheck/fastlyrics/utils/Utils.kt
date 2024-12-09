package io.github.teccheck.fastlyrics.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonElement
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.Deezer
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.api.provider.LrcLib
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.api.provider.Netease
import io.github.teccheck.fastlyrics.api.provider.PetitLyrics
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.exceptions.NoMusicPlayingException
import io.github.teccheck.fastlyrics.exceptions.ParseException
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.model.SyncedLyrics

object Utils {
    fun <T, E> result(value: T?, exception: E): Result<T, E> {
        return if (value == null) Failure(exception)
        else Success(value)
    }

    fun View.setVisible(visible: Boolean) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun copyToClipboard(context: Context, title: String, text: String) =
        ContextCompat.getSystemService(context, ClipboardManager::class.java)
            ?.setPrimaryClip(ClipData.newPlainText(title, text))

    fun openLink(context: Context, link: String) =
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

    fun share(context: Context, songTitle: String, artist: String, text: String) {
        val title = context.getString(R.string.share_title, songTitle, artist)
        ShareCompat.IntentBuilder(context).setText(text).setType("text/plain")
            .setChooserTitle(title).setSubject(title).startChooser()
    }

    fun SongWithLyrics.getLyrics(preferSynced: Boolean = false): String {
        if (preferSynced && lyricsSynced != null) return lyricsSynced

        if (lyricsPlain != null) return lyricsPlain

        if (lyricsSynced != null) return SyncedLyrics.parseLrcToList(lyricsSynced)
            .joinToString("\n") { it.text }

        return ""
    }

    fun JsonElement.asStringOrNull() : String? {
        if (this.isJsonNull) return null
        return this.asString
    }

    fun <T> Array<T>.swap(first: Int, second: Int) {
        val tmp = this[second]
        this[second] = this[first]
        this[first] = tmp
    }

    @DrawableRes
    fun getProviderIconRes(provider: LyricsProvider) = when (provider) {
        Genius -> R.drawable.genius
        Deezer -> R.drawable.deezer
        LrcLib -> R.drawable.lrclib
        PetitLyrics -> R.drawable.petitlyrics
        Netease -> R.drawable.netease
        else -> R.drawable.fastlyrics
    }

    @StringRes
    fun getProviderNameRes(provider: LyricsProvider) = when (provider) {
        Genius -> R.string.source_genius
        Deezer -> R.string.source_deezer
        LrcLib -> R.string.source_lrclib
        PetitLyrics -> R.string.source_petitlyrics
        Netease -> R.string.source_netease
        else -> R.string.app_name
    }

    @StringRes
    fun getProviderUrlRes(provider: LyricsProvider) = when (provider) {
        Genius -> R.string.source_url_genius
        Deezer -> R.string.source_url_deezer
        LrcLib -> R.string.source_url_lrclib
        PetitLyrics -> R.string.source_url_petitlyrics
        Netease -> R.string.source_url_netease
        else -> null
    }

    @StringRes
    fun getErrorTextRes(exception: LyricsApiException) = when (exception) {
        is LyricsNotFoundException -> R.string.lyrics_not_found
        is NetworkException -> R.string.lyrics_network_exception
        is ParseException -> R.string.lyrics_parse_exception
        is NoMusicPlayingException -> R.string.no_song_playing
        else -> R.string.lyrics_unknown_error
    }

    @DrawableRes
    fun getErrorIconRes(exception: LyricsApiException) = when (exception) {
        is NoMusicPlayingException -> R.drawable.outline_music_off_24
        else -> R.drawable.baseline_error_outline_24
    }
}