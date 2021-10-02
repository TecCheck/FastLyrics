package io.github.teccheck.fastlyrics.ui.lyrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LyricsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Lyrics"
    }
    val text: LiveData<String> = _text
}