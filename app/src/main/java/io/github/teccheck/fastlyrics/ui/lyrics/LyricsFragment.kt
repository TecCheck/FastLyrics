package io.github.teccheck.fastlyrics.ui.lyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.databinding.FragmentLyricsBinding
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.exceptions.NoMusicPlayingException
import io.github.teccheck.fastlyrics.exceptions.ParseException
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

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

        lyricsViewModel.songMeta.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Success -> {
                    binding.header.visibility = View.VISIBLE
                    binding.errorView.visibility = View.GONE
                    binding.textSongTitle.text = result.value.title
                    binding.textSongArtist.text = result.value.artist
                    binding.imageSongArt.setImageBitmap(result.value.art)
                }

                is Failure -> {
                    displayError(result.reason)
                }
            }
        }

        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner) { result ->
            binding.refreshLayout.isRefreshing = false

            when (result) {
                is Success -> {
                    binding.header.visibility = View.VISIBLE
                    binding.lyricsView.visibility = View.VISIBLE
                    binding.errorView.visibility = View.GONE

                    binding.textSongTitle.text = result.value.title
                    binding.textSongArtist.text = result.value.artist
                    binding.textLyrics.text = result.value.lyrics
                    Picasso.get().load(result.value.artUrl).into(binding.imageSongArt)
                }

                is Failure -> {
                    displayError(result.reason)
                }
            }
        }

        binding.refreshLayout.setColorSchemeResources(
            R.color.theme_primary,
            R.color.theme_secondary
        )
        binding.refreshLayout.setOnRefreshListener { loadLyricsForCurrentSong() }

        arguments?.let {
            if (it.containsKey(ARG_TITLE) && it.containsKey(ARG_ARTIST)) {
                autoLoad = false
                binding.refreshLayout.isEnabled = false

                lyricsViewModel.loadLyricsForSongFromStorage(
                    it.getString(ARG_TITLE, ""),
                    it.getString(ARG_ARTIST, "")
                )
            }
        }

        val notificationAccess =
            context?.let { DummyNotificationListenerService.canAccessNotifications(it) } ?: false

        if (autoLoad && notificationAccess) {
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
            binding.refreshLayout.isRefreshing = true
            val success = lyricsViewModel.loadLyricsForCurrentSong(it)

            if (!success)
                binding.refreshLayout.isRefreshing = false
        }
    }

    private fun displayError(exception: LyricsApiException) {
        binding.lyricsView.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE

        binding.errorText.text = getErrorTextForApiException(exception)
        binding.errorIcon.setImageDrawable(getErrorIconForApiException(exception))

        val headerVisibility = if (exception is NoMusicPlayingException) {
            View.GONE
        } else {
            View.VISIBLE
        }

        binding.header.visibility = headerVisibility
    }

    private fun getErrorTextForApiException(exception: LyricsApiException): String =
        when (exception) {
            is LyricsNotFoundException -> getString(R.string.lyrics_not_found)
            is NetworkException -> getString(R.string.lyrics_network_exception)
            is ParseException -> getString(R.string.lyrics_parse_exception)
            is NoMusicPlayingException -> getString(R.string.no_song_playing)
            else -> getString(R.string.lyrics_unknown_error)
        }

    private fun getErrorIconForApiException(exception: LyricsApiException) =
        when (exception) {
            is NoMusicPlayingException -> ResourcesCompat.getDrawable(resources, R.drawable.outline_music_off_24, null)
            else -> ResourcesCompat.getDrawable(resources, R.drawable.baseline_error_outline_24, null)
        }


    companion object {
        private const val TAG = "LyricsFragment"

        const val ARG_TITLE = "title"
        const val ARG_ARTIST = "artist"
    }
}