package io.github.teccheck.fastlyrics.ui.saved

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.model.SongWithLyrics

class RecyclerAdapter(private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var songs: List<SongWithLyrics> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageArt: ImageView
        val textTitle: TextView
        val textArtist: TextView

        init {
            imageArt = view.findViewById(R.id.image_song_art)
            textTitle = view.findViewById(R.id.text_song_title)
            textArtist = view.findViewById(R.id.text_song_artist)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_song, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val song = songs[position]
        viewHolder.itemView.setOnClickListener { itemClickListener.onItemClick(song) }
        viewHolder.textTitle.text = song.title
        viewHolder.textArtist.text = song.artist
        Picasso.get().load(song.artUrl).into(viewHolder.imageArt)
    }

    override fun getItemCount() = songs.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSongs(songs: List<SongWithLyrics>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: SongWithLyrics)
    }
}

