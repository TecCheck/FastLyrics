package io.github.teccheck.fastlyrics.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.github.teccheck.fastlyrics.BuildConfig
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)

        binding.textVersion.text = BuildConfig.VERSION_NAME
        binding.layoutSourceCode.setOnClickListener { openLink(R.string.source_code_url) }
        binding.layoutSourceGenius.setOnClickListener { openLink(R.string.source_url_genius) }

        return binding.root
    }

    private fun openLink(@StringRes resId: Int) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(resId)))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "AboutFragment"
    }
}