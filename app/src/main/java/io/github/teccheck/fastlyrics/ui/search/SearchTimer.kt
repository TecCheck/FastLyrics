package io.github.teccheck.fastlyrics.ui.search

import android.os.Handler
import android.os.Looper

class SearchTimer(private val queryDispatcher: QueryDispatcher) {
    private val handler = Handler(Looper.getMainLooper())
    private var query: String? = null
    private var timerRunning = false

    fun setQuery(query: String?) {
        this.query = query
        if (!timerRunning && query != null) {
            timerRunning = true
            handler.postDelayed(this::dispatchQuery, TIMER_DELAY)
        }
    }

    private fun dispatchQuery() {
        timerRunning = false
        query?.let { queryDispatcher.dispatch(it) }
    }

    fun interface QueryDispatcher {
        fun dispatch(query: String)
    }

    companion object {
        private const val TIMER_DELAY = 800.toLong()
    }
}