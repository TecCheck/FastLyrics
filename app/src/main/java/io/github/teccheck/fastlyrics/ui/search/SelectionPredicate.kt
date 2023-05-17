package io.github.teccheck.fastlyrics.ui.search

import androidx.recyclerview.selection.SelectionTracker

class SelectionPredicate : SelectionTracker.SelectionPredicate<Long>() {
    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
        return false
    }

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
        return false
    }

    override fun canSelectMultiple(): Boolean {
        return false
    }
}