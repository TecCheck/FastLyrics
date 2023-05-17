package io.github.teccheck.fastlyrics.ui.search

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class DetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y) ?: return null
        val viewHolder = recyclerView.findContainingViewHolder(view) as RecyclerAdapter.ViewHolder?
            ?: return null

        return SearchResultDetails(viewHolder.adapterPosition, viewHolder.itemId)
    }

    class SearchResultDetails(private val position: Int, private val itemId: Long?) :
        ItemDetails<Long>() {

        override fun getPosition() = position

        override fun getSelectionKey() = itemId
    }
}