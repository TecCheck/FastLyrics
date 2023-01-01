package io.github.teccheck.fastlyrics.ui.lyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import io.github.teccheck.fastlyrics.databinding.FragmentLyricsBinding

class LyricsFragment : Fragment() {

    private lateinit var lyricsViewModel: LyricsViewModel
    private var _binding: FragmentLyricsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var autoLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lyricsViewModel = ViewModelProvider(this).get(LyricsViewModel::class.java)
        _binding = FragmentLyricsBinding.inflate(inflater, container, false)

        lyricsViewModel.songMeta.observe(viewLifecycleOwner) {
            binding.textSongTitle.text = it.title
            binding.textSongArtist.text = it.artist
            binding.imageSongArt.setImageBitmap(it.art)
        }

        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner) {
            binding.textSongTitle.text = it.title
            binding.textSongArtist.text = it.artist
            binding.textLyrics.text = it.lyrics
            Picasso.get().load(it.artUrl).into(binding.imageSongArt)
        }

        lyricsViewModel.loading.observe(viewLifecycleOwner) {
            binding.refreshLayout.isRefreshing = it
        }

        binding.refreshLayout.setOnRefreshListener { loadLyricsForCurrentSong() }

        if (autoLoad) {
            autoLoad = false
            loadLyricsForCurrentSong()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadLyricsForCurrentSong() {
        context?.let {
            lyricsViewModel.loadLyricsForCurrentSong(it)
        }
    }
}