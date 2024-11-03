package io.github.teccheck.fastlyrics.utils

import androidx.lifecycle.MutableLiveData

class DebouncedMutableLiveData<T> : MutableLiveData<T>() {
    override fun setValue(value: T) {
        if (value == getValue()) return

        super.setValue(value)
    }
}