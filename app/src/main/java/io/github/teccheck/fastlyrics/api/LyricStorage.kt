package io.github.teccheck.fastlyrics.api

import android.util.Log
import com.google.gson.Gson
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import java.io.File

object LyricStorage {

    private const val TAG = "LyricsStorage"
    private const val FILENAME = "lyrics.json"

    private var songs = mutableListOf<SongWithLyrics>()
    private var changed = false

    init {
        read()
    }

    fun read() {
        val file = File(FILENAME)
        if (!file.exists())
            return

        val json = file.readText()
        val gson = Gson()
        songs = gson.fromJson(json, songs.javaClass)
    }

    fun write() {
        if (!changed) {
            Log.w(TAG, "Tried to write without changes")
        }

        val gson = Gson()
        val json = gson.toJson(songs)
        val file = File(FILENAME)

        if (!file.exists())
            file.createNewFile()

        file.writeText(json)
        changed = false;
    }

    fun store(song: SongWithLyrics) {
        if (!songs.contains(song)) {
            songs.add(song)
            changed = true
        }
    }
}