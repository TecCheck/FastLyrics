package io.github.teccheck.fastlyrics.ui.viewlyrics

import android.content.Intent
import android.net.Uri
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
import io.github.teccheck.fastlyrics.model.SearchResult

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
            if (result is Success) {
                binding.header.textSongTitle.text = result.value.title
                binding.header.textSongArtist.text = result.value.artist
                binding.lyricsView.textLyrics.text = result.value.lyrics
                Picasso.get().load(result.value.artUrl).into(binding.header.imageSongArt)

                binding.lyricsView.footer.setOnClickListener { openLink(result.value.sourceUrl) }
            }
        }

        arguments?.let {
            if (it.containsKey(ARG_SONG_ID)) {
                lyricsViewModel.loadLyricsForSongFromStorage(it.getLong(ARG_SONG_ID, 0))
            } else if (it.containsKey(ARG_SEARCH_RESULT)) {
                binding.refreshLayout.isRefreshing = true
                val searchResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getSerializable(ARG_SEARCH_RESULT, SearchResult::class.java)
                } else {
                    it.getSerializable(ARG_SEARCH_RESULT) as SearchResult
                } ?: return@let

                lyricsViewModel.loadLyricsForSearchResult(searchResult)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openLink(link: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

    companion object {
        private const val TAG = "ViewLyricsFragment"
        const val ARG_SONG_ID = "song_id"
        const val ARG_SEARCH_RESULT = "search_result"
    }
}