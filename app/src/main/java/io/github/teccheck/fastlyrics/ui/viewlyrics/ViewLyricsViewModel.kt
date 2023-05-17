package io.github.teccheck.fastlyrics.ui.viewlyrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.api.LyricsApi
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics

class ViewLyricsViewModel : ViewModel() {

    private val _songWithLyrics = MutableLiveData<Result<SongWithLyrics, LyricsApiException>>()

    val songWithLyrics: LiveData<Result<SongWithLyrics, LyricsApiException>> = _songWithLyrics

    fun loadLyricsForSongFromStorage(songId: Long) =
        LyricStorage.getSongAsync(songId, _songWithLyrics)

    fun loadLyricsForSearchResult(searchResult: SearchResult) =
        LyricsApi.getLyricsAsync(searchResult, _songWithLyrics)

    companion object {
        private const val TAG = "ViewLyricsViewModel"
    }
}