package io.github.teccheck.fastlyrics.api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import java.io.File
import java.util.concurrent.Executors

object LyricStorage {
    private const val TAG = "LyricsStorage"
    private const val FILENAME = "lyrics.json"

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var file: File
    private var songs = mutableListOf<SongWithLyrics>()
    private var changed = false

    fun init(context: Context) {
        file = context.filesDir.resolve(FILENAME)
        Log.d(TAG, "File ${file.absolutePath}")
        readSync()
    }

    fun read() {
        executor.submit { readSync() }
    }

    fun write() {
        executor.submit { writeSync() }
    }

    fun store(song: SongWithLyrics, write: Boolean = false) {
        executor.submit {
            storeSync(song)
            if (write)
                writeSync()
        }
    }

    private fun readSync() {
        if (!file.exists())
            return

        songs = Gson().fromJson(file.readText(), songs.javaClass)
    }

    private fun writeSync() {
        if (!changed) {
            Log.w(TAG, "Tried to write without changes")
        }

        if (!file.exists())
            file.createNewFile()

        file.writeText(Gson().toJson(songs))
        changed = false;
    }

    private fun storeSync(song: SongWithLyrics) {
        if (!songs.contains(song)) {
            songs.add(song)
            changed = true
        }
    }
}