package io.github.teccheck.fastlyrics.ui.viewlyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.databinding.FragmentViewLyricsBinding

class ViewLyricsFragment : Fragment() {

    private lateinit var lyricsViewModel: ViewLyricsViewModel
    private var _binding: FragmentViewLyricsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        lyricsViewModel = ViewModelProvider(this)[ViewLyricsViewModel::class.java]
        _binding = FragmentViewLyricsBinding.inflate(inflater, container, false)

        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner) { result ->
            if (result is Success) {
                binding.textSongTitle.text = result.value.title
                binding.textSongArtist.text = result.value.artist
                binding.textLyrics.text = result.value.lyrics
                Picasso.get().load(result.value.artUrl).into(binding.imageSongArt)
            }
        }

        arguments?.let {
            if (it.containsKey(ARG_SONG_ID)) {
                lyricsViewModel.loadLyricsForSongFromStorage(it.getLong(ARG_SONG_ID, 0))
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ViewLyricsFragment"
        const val ARG_SONG_ID = "song_id"
    }
}