package io.github.teccheck.fastlyrics.ui.saved

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.model.SongWithLyrics

class RecyclerAdapter :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var songs: List<SongWithLyrics> = listOf()
    private var selectionTracker: SelectionTracker<Long>? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageArt: ImageView
        private val textTitle: TextView
        private val textArtist: TextView
        private val selectionIcon: ImageView

        private var song: SongWithLyrics? = null

        init {
            imageArt = view.findViewById(R.id.image_song_art)
            textTitle = view.findViewById(R.id.text_song_title)
            textArtist = view.findViewById(R.id.text_song_artist)
            selectionIcon = view.findViewById(R.id.selection_icon)
        }

        fun bind(song: SongWithLyrics, selected: Boolean) {
            this.song = song
            textTitle.text = song.title
            textArtist.text = song.artist
            Picasso.get().load(song.artUrl).into(imageArt)
            selectionIcon.visibility = if (selected) View.VISIBLE else View.GONE
        }

        fun getSongId() = song?.id
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_song, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val song = songs[position]
        val selected = selectionTracker?.isSelected(position.toLong()) ?: false
        viewHolder.bind(song, selected)
    }

    override fun getItemCount() = songs.size

    override fun getItemId(position: Int) = position.toLong()

    @SuppressLint("NotifyDataSetChanged")
    fun setSongs(songs: List<SongWithLyrics>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun setSelectionTracker(selectionTracker: SelectionTracker<Long>) {
        this.selectionTracker = selectionTracker
    }
}

