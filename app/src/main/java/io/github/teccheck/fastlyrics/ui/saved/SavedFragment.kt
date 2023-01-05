package io.github.teccheck.fastlyrics.ui.saved

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.databinding.FragmentSavedBinding
import io.github.teccheck.fastlyrics.model.SongWithLyrics

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val itemClickListener = object : RecyclerAdapter.OnItemClickListener {
        override fun onItemClick(item: SongWithLyrics) {
            Log.d(TAG, "Click on ${item.title} - ${item.artist}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)

        binding.recyclerView.adapter = RecyclerAdapter(LyricStorage.getLyrics(), itemClickListener)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

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