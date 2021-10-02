package io.github.teccheck.fastlyrics.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.github.teccheck.fastlyrics.databinding.FragmentSavedBinding

class SavedFragment : Fragment() {

    private lateinit var savedViewModel: SavedViewModel
    private var _binding: FragmentSavedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        savedViewModel = ViewModelProvider(this).get(SavedViewModel::class.java)

        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        savedViewModel.text.observe(viewLifecycleOwner, { textView.text = it })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}