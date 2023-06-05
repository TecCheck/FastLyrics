package io.github.teccheck.fastlyrics.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R

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
        ShareCompat.IntentBuilder(requireContext())
            .setText(text)
            .setType("text/plain")
            .setChooserTitle(title)
            .setSubject(title)
            .startChooser()
    }

}