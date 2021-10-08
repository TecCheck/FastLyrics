package io.github.teccheck.fastlyrics.ui.lyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.github.teccheck.fastlyrics.databinding.FragmentLyricsBinding

class LyricsFragment : Fragment() {

    private lateinit var lyricsViewModel: LyricsViewModel
    private var _binding: FragmentLyricsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lyricsViewModel = ViewModelProvider(this).get(LyricsViewModel::class.java)
        _binding = FragmentLyricsBinding.inflate(inflater, container, false)

        lyricsViewModel.text.observe(viewLifecycleOwner, { binding.textLyrics.text = it })

        return binding.root;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}