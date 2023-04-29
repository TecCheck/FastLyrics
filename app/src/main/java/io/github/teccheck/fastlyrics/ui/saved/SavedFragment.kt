package io.github.teccheck.fastlyrics.ui.saved

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.databinding.FragmentSavedBinding
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.ui.lyrics.LyricsFragment

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val itemClickListener = object : RecyclerAdapter.OnItemClickListener {
        override fun onItemClick(item: SongWithLyrics) {
            val bundle = Bundle()
            bundle.putString(LyricsFragment.ARG_TITLE, item.title)
            bundle.putString(LyricsFragment.ARG_ARTIST, item.artist)
            findNavController().navigate(R.id.nav_lyrics_view_saved, bundle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this)[SavedViewModel::class.java]
        _binding = FragmentSavedBinding.inflate(inflater, container, false)

        val adapter = RecyclerAdapter(itemClickListener)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.songs.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "test")
            if (result is Success) {
                adapter.setSongs(result.value)
            }
        }

        viewModel.fetchSongs()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SavedFragment"
    }
}