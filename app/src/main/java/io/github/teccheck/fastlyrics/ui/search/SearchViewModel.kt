package io.github.teccheck.fastlyrics.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.api.LyricsApi
import io.github.teccheck.fastlyrics.model.SearchResult

class SearchViewModel : ViewModel() {
    private val _searchResults = MutableLiveData<Result<List<SearchResult>, LyricsApiException>>()
    val searchResults: LiveData<Result<List<SearchResult>, LyricsApiException>> = _searchResults

    fun search(query: String) {
        LyricsApi.search(query, _searchResults)
    }
}