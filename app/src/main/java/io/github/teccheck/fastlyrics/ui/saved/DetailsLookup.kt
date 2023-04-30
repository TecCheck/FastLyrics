package io.github.teccheck.fastlyrics.ui.saved

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class DetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y) ?: return null
        val viewHolder = recyclerView.findContainingViewHolder(view) as RecyclerAdapter.ViewHolder?
            ?: return null

        return SongWithLyricsDetails(
            recyclerView.getChildAdapterPosition(view), viewHolder.getSongId()
        )
    }

    class SongWithLyricsDetails(private val position: Int, private val key: Long?) :
        ItemDetails<Long>() {
        override fun getPosition(): Int {
            return position
        }

        override fun getSelectionKey(): Long? {
            return key
        }
    }
}