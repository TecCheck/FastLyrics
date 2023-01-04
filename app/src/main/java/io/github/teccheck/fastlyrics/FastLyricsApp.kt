package io.github.teccheck.fastlyrics

import android.app.Application
import io.github.teccheck.fastlyrics.api.LyricStorage

class FastLyricsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LyricStorage.init(this)
    }

}