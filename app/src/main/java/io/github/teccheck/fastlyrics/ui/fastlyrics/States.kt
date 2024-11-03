package io.github.teccheck.fastlyrics.ui.fastlyrics

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.utils.Utils
import io.github.teccheck.fastlyrics.utils.Utils.getLyrics

open class UiState(
    val showHeader: Boolean,
    val showError: Boolean,
    val showText: Boolean,
    val isRefreshing: Boolean = false,
    val startRefresh: Boolean = false,
) {
    // Header
    open fun getSongTitle(): String = ""
    open fun getSongArtist(): String = ""
    open fun getArtBitmap(): Bitmap? = null
    open fun getArtUrl(): String? = null

    // Lyrics
    open fun getSongProvider(): LyricsProvider? = null
    open fun getSourceUrl(): String = ""
    open fun getLyrics(): String = ""
    open fun getSyncedLyrics(): String? = null
    fun hasSyncedLyrics() = getSyncedLyrics() != null

    // Error
    @StringRes
    open fun getErrorText(): Int? = null

    @DrawableRes
    open fun getErrorIcon(): Int? = null
}

class StartupState : UiState(false, false, false, true, true)

class NoMusicState : UiState(false, true, false) {
    override fun getErrorIcon() = R.drawable.outline_music_off_24
    override fun getErrorText() = R.string.no_song_playing
}

class ErrorState(private val songMeta: SongMeta?, private val exception: LyricsApiException) :
    UiState(true, true, false) {
    override fun getErrorIcon() = Utils.getErrorIconRes(exception)
    override fun getErrorText() = Utils.getErrorTextRes(exception)

    override fun getSongTitle() = songMeta?.title ?: ""
    override fun getSongArtist() = songMeta?.artist ?: ""
    override fun getArtBitmap() = songMeta?.art
}

class LoadingState(private val songMeta: SongMeta) : UiState(true, false, false, true) {
    override fun getSongTitle() = songMeta.title
    override fun getSongArtist() = songMeta.artist ?: ""
    override fun getArtBitmap() = songMeta.art
}

class TextState(private val songMeta: SongMeta?, private val songWithLyrics: SongWithLyrics) :
    UiState(true, false, true) {
    override fun getSongTitle() = songWithLyrics.title
    override fun getSongArtist() = songWithLyrics.artist
    override fun getArtUrl() = songWithLyrics.artUrl
    override fun getArtBitmap() = songMeta?.art

    override fun getSongProvider() = LyricsProvider.getProviderByName(songWithLyrics.provider)
    override fun getSourceUrl() = songWithLyrics.sourceUrl
    override fun getLyrics() = songWithLyrics.getLyrics(false)
    override fun getSyncedLyrics() = songWithLyrics.lyricsSynced
}