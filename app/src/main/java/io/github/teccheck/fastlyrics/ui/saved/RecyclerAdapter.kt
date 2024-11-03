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
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.utils.PlaceholderDrawable
import io.github.teccheck.fastlyrics.utils.Utils

class RecyclerAdapter :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var songs: List<SongWithLyrics> = listOf()
    private var selectionTracker: SelectionTracker<Long>? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageArt: ImageView = view.findViewById(R.id.image_song_art)
        private val textTitle: TextView = view.findViewById(R.id.text_song_title)
        private val textArtist: TextView = view.findViewById(R.id.text_song_artist)
        private val iconProvider: ImageView = view.findViewById(R.id.provider_icon)
        private val selectionIcon: ImageView = view.findViewById(R.id.selection_icon)

        private var song: SongWithLyrics? = null

        fun bind(song: SongWithLyrics, selected: Boolean) {
            this.song = song
            textTitle.text = song.title
            textArtist.text = song.artist

            val picasso = Picasso.get().load(song.artUrl)

            LyricsProvider.getProviderByName(song.provider)?.let { provider ->
                Utils.getProviderIconRes(provider).let {
                    iconProvider.setImageResource(it)
                    picasso.placeholder(PlaceholderDrawable(imageArt.context, it))
                }
            }

            picasso.into(imageArt)
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

