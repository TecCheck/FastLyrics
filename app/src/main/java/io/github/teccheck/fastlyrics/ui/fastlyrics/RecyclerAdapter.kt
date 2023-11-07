package io.github.teccheck.fastlyrics.ui.fastlyrics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.model.SyncedLyrics

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var syncedLyrics: SyncedLyrics? = null
    private var activeLine = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView = view as TextView
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_synced_line, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = syncedLyrics?.getLineByIndex(position)

        val ta = if (activeLine == position) {
            R.style.AppTheme_TextView_ActiveLyrics
        } else {
            R.style.AppTheme_TextView_InactiveLyrics
        }

        viewHolder.textView.setTextAppearance(ta)
    }

    override fun getItemCount() = syncedLyrics?.getLineCount() ?: 0

    override fun getItemId(position: Int) = position.toLong()

    fun setSyncedLyrics(syncedLyrics: SyncedLyrics?) {
        this.syncedLyrics = syncedLyrics
        notifyDataSetChanged()
    }

    fun setTime(time: Long): Int? {
        val index = syncedLyrics?.getLineIndex(time) ?: return null

        if (index == activeLine) return null

        val oldActive = activeLine
        activeLine = index
        notifyItemChanged(index)
        notifyItemChanged(oldActive)
        return index
    }
}

