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
            viewHolder.adapterPosition,
            viewHolder.itemId,
            viewHolder.getSongId()
        )
    }

    class SongWithLyricsDetails(
        private val position: Int, private val itemId: Long?, val songId: Long?
    ) : ItemDetails<Long>() {

        override fun getPosition() = position

        override fun getSelectionKey() = itemId
    }
}