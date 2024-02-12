package io.github.teccheck.fastlyrics.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.Deezer
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.exceptions.NoMusicPlayingException
import io.github.teccheck.fastlyrics.exceptions.ParseException

object Utils {
    fun <T, E> result(value: T?, exception: E): Result<T, E> {
        return if (value == null) Failure(exception)
        else Success(value)
    }

    fun Fragment.copyToClipboard(title: String, text: String) {
        val clipboard =
            ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText(title, text)
        clipboard?.setPrimaryClip(clip)
    }

    fun Fragment.openLink(link: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

    fun Fragment.share(songTitle: String, artist: String, text: String) {
        val title = getString(R.string.share_title, songTitle, artist)
        ShareCompat.IntentBuilder(requireContext()).setText(text).setType("text/plain")
            .setChooserTitle(title).setSubject(title).startChooser()
    }

    @DrawableRes
    fun getProviderIconRes(provider: LyricsProvider) = when (provider) {
        Genius -> R.drawable.genius
        Deezer -> R.drawable.deezer
        else -> R.drawable.fastlyrics
    }

    @StringRes
    fun getProviderNameRes(provider: LyricsProvider) = when (provider) {
        Genius -> R.string.source_genius
        Deezer -> R.string.source_deezer
        else -> R.string.app_name
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