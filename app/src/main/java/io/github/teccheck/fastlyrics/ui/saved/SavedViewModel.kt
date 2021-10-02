package io.github.teccheck.fastlyrics.ui.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SavedViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Saved Lyrics"
    }
    val text: LiveData<String> = _text
}