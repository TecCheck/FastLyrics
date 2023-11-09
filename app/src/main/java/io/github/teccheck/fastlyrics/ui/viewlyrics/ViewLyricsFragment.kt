package io.github.teccheck.fastlyrics.ui.viewlyrics

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.databinding.FragmentViewLyricsBinding
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.model.SyncedLyrics
import io.github.teccheck.fastlyrics.utils.Utils.copyToClipboard
import io.github.teccheck.fastlyrics.utils.Utils.openLink
import io.github.teccheck.fastlyrics.utils.Utils.share

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

        binding.refreshLayout.isEnabled = false
        binding.refreshLayout.setColorSchemeResources(
            R.color.theme_primary, R.color.theme_secondary
        )

        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner) { result ->
            binding.refreshLayout.isRefreshing = false
            if (result is Success) displaySongWithLyrics(result.value)
        }

        arguments?.let {
            if (it.containsKey(ARG_SONG_ID)) {
                lyricsViewModel.loadLyricsForSongFromStorage(it.getLong(ARG_SONG_ID, 0))
            } else if (it.containsKey(ARG_SEARCH_RESULT)) {
                binding.refreshLayout.isRefreshing = true
                val result = getSearchResult(it) ?: return@let
                lyricsViewModel.loadLyricsForSearchResult(result)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displaySongWithLyrics(song: SongWithLyrics) {
        binding.header.textSongTitle.text = song.title
        binding.header.textSongArtist.text = song.artist
        displayLyrics(song)
        Picasso.get().load(song.artUrl).into(binding.header.imageSongArt)

        binding.lyricsView.source.setOnClickListener { openLink(song.sourceUrl) }
        binding.lyricsView.copy.setOnClickListener {
            copyToClipboard(
                getString(R.string.lyrics_clipboard_label), song.lyrics
            )
        }
        binding.lyricsView.share.setOnClickListener {
            share(
                song.title,
                song.artist,
                song.lyrics
            )
        }
    }

    private fun displayLyrics(song: SongWithLyrics) {
        binding.lyricsView.textLyrics.text = if (song.type == LyricsType.LRC) {
            val syncedLyrics = SyncedLyrics.parseLrc(song.lyrics)
            syncedLyrics?.getFullText() ?: ""
        } else {
            song.lyrics
        }
    }

    private fun getSearchResult(args: Bundle): SearchResult? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.getSerializable(ARG_SEARCH_RESULT, SearchResult::class.java)
        } else {
            @Suppress("DEPRECATION")
            args.getSerializable(ARG_SEARCH_RESULT) as SearchResult
        }
    }

    companion object {
        private const val TAG = "ViewLyricsFragment"
        const val ARG_SONG_ID = "song_id"
        const val ARG_SEARCH_RESULT = "search_result"
    }
}