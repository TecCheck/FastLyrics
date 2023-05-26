package io.github.teccheck.fastlyrics.utils

import android.content.ClipData
import android.content.ClipboardManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

object Utils {
    fun <T, E> result(value: T?, exception: E): Result<T, E> {
        return if (value == null)
            Failure(exception)
        else
            Success(value)
    }

    fun Fragment.copyToClipboard(title: String, text: String) {
        val clipboard =
            ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText(title, text)
        clipboard?.setPrimaryClip(clip)
    }
}