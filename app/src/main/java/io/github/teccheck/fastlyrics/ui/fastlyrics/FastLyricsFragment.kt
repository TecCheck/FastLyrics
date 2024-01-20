package io.github.teccheck.fastlyrics.ui.fastlyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.Settings
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.databinding.FragmentFastLyricsBinding
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.exceptions.NoMusicPlayingException
import io.github.teccheck.fastlyrics.exceptions.ParseException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.model.SyncedLyrics
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService
import io.github.teccheck.fastlyrics.utils.Utils
import io.github.teccheck.fastlyrics.utils.Utils.copyToClipboard
import io.github.teccheck.fastlyrics.utils.Utils.openLink
import io.github.teccheck.fastlyrics.utils.Utils.share


class FastLyricsFragment : Fragment() {

    private lateinit var lyricsViewModel: FastLyricsViewModel
    private var _binding: FragmentFastLyricsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recyclerAdapter: RecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        lyricsViewModel = ViewModelProvider(this)[FastLyricsViewModel::class.java]
        _binding = FragmentFastLyricsBinding.inflate(inflater, container, false)

        binding.lyricsView.container.visibility = View.GONE

        lyricsViewModel.songMeta.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Success -> displaySongMeta(result.value)
                is Failure -> displayError(result.reason)
            }
        }

        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner) { result ->
            binding.header.syncedLyricsSwitch.isChecked = false
            binding.refreshLayout.isRefreshing = false
            displayResult(result)
        }

        lyricsViewModel.songWithLyricsSynced.observe(viewLifecycleOwner) { result ->
            binding.refreshLayout.isRefreshing = false

            when (result) {
                is Success -> {
                    lyricsViewModel.syncedLyricsAvailable = true
                    binding.header.syncedLyricsAvailable.visibility = View.VISIBLE
                }
                is Failure -> {
                    binding.header.syncedLyricsAvailable.visibility = View.GONE
                    binding.header.syncedLyricsSwitch.isChecked = false
                }
            }
        }

        lyricsViewModel.songPosition.observe(viewLifecycleOwner, this::setTime)

        binding.refreshLayout.setColorSchemeResources(
            R.color.theme_primary, R.color.theme_secondary
        )
        binding.refreshLayout.setOnRefreshListener { loadLyricsForCurrentSong() }

        binding.header.syncedLyricsSwitch.setOnCheckedChangeListener { _, checked ->
            val song = if (checked && lyricsViewModel.syncedLyricsAvailable) {
                lyricsViewModel.songWithLyricsSynced.value
            } else {
                lyricsViewModel.songWithLyrics.value
            }

            song?.let { displayResult(it) }
        }

        val notificationAccess =
            context?.let { DummyNotificationListenerService.canAccessNotifications(it) } ?: false

        if (notificationAccess) {
            loadLyricsForCurrentSong()
            context?.let {
                lyricsViewModel.setupSongMetaListener()
                lyricsViewModel.autoRefresh = Settings(it).getIsAutoRefreshEnabled()
            }
        }

        recyclerAdapter = RecyclerAdapter()
        binding.lyricsView.syncedRecycler.adapter = recyclerAdapter
        binding.lyricsView.syncedRecycler.layoutManager = LinearLayoutManager(context)

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

            if (!success) binding.refreshLayout.isRefreshing = false
        }
    }

    private fun displayResult(result: Result<SongWithLyrics, LyricsApiException>) {
        when (result) {
            is Success -> displaySongWithLyrics(result.value)
            is Failure -> displayError(result.reason)
        }
    }

    private fun displaySongMeta(songMeta: SongMeta) {
        binding.header.container.visibility = View.VISIBLE
        binding.errorView.container.visibility = View.GONE

        binding.header.textSongTitle.text = songMeta.title
        binding.header.textSongArtist.text = songMeta.artist
        binding.header.imageSongArt.setImageBitmap(songMeta.art)
    }

    private fun displaySongWithLyrics(song: SongWithLyrics) {
        binding.header.container.visibility = View.VISIBLE
        binding.lyricsView.container.visibility = View.VISIBLE
        binding.errorView.container.visibility = View.GONE

        binding.header.textSongTitle.text = song.title
        binding.header.textSongArtist.text = song.artist
        displayLyrics(song)
        Picasso.get().load(song.artUrl).into(binding.header.imageSongArt)

        val provider = LyricsProvider.getProviderByName(song.provider)
        provider?.let {
            val providerIconRes = Utils.getProviderIconRes(it)!!
            val icon = AppCompatResources.getDrawable(requireContext(), providerIconRes)
            binding.lyricsView.source.setIconResource(providerIconRes)
            binding.lyricsView.textLyricsProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon,
                null,
                null,
                null
            )

            val nameRes = Utils.getProviderNameRes(it)!!
            val name = getString(nameRes)
            binding.lyricsView.source.text = name
            binding.lyricsView.textLyricsProvider.text = name
        }

        binding.lyricsView.source.setOnClickListener { openLink(song.sourceUrl) }
        binding.lyricsView.copy.setOnClickListener {
            copyToClipboard(
                getString(R.string.lyrics_clipboard_label), song.lyrics
            )
        }
        binding.lyricsView.share.setOnClickListener {
            share(
                song.title, song.artist, song.lyrics
            )
        }
    }

    private fun displayLyrics(song: SongWithLyrics) {
        if (song.type == LyricsType.RAW_TEXT) {
            binding.lyricsView.syncedRecycler.visibility = View.GONE
            binding.lyricsView.textLyrics.visibility = View.VISIBLE

            binding.lyricsView.textLyrics.text = song.lyrics
            recyclerAdapter.setSyncedLyrics(null)
            lyricsViewModel.setupPositionPolling(false)
        } else if (song.type == LyricsType.LRC) {
            binding.lyricsView.textLyrics.visibility = View.GONE
            binding.lyricsView.syncedRecycler.visibility = View.VISIBLE

            SyncedLyrics.parseLrc(song.lyrics)?.let { recyclerAdapter.setSyncedLyrics(it) }
            binding.lyricsView.textLyrics.text = ""
            lyricsViewModel.setupPositionPolling(true)
        }
    }

    private fun displayError(exception: LyricsApiException) {
        binding.lyricsView.container.visibility = View.GONE
        binding.errorView.container.visibility = View.VISIBLE
        binding.header.container.visibility = if (exception is NoMusicPlayingException) {
            View.GONE
        } else {
            View.VISIBLE
        }

        binding.errorView.errorText.text = getErrorTextForApiException(exception)
        binding.errorView.errorIcon.setImageDrawable(getErrorIconForApiException(exception))
    }

    private fun getErrorTextForApiException(exception: LyricsApiException): String =
        when (exception) {
            is LyricsNotFoundException -> getString(R.string.lyrics_not_found)
            is NetworkException -> getString(R.string.lyrics_network_exception)
            is ParseException -> getString(R.string.lyrics_parse_exception)
            is NoMusicPlayingException -> getString(R.string.no_song_playing)
            else -> getString(R.string.lyrics_unknown_error)
        }

    private fun getErrorIconForApiException(exception: LyricsApiException) = when (exception) {
        is NoMusicPlayingException -> ResourcesCompat.getDrawable(
            resources, R.drawable.outline_music_off_24, null
        )

        else -> ResourcesCompat.getDrawable(
            resources, R.drawable.baseline_error_outline_24, null
        )
    }

    private fun setTime(time: Long) {
        val index = recyclerAdapter.setTime(time) ?: return
        val recycler = binding.lyricsView.syncedRecycler

        recycler.post {
            val recyclerPos = binding.lyricsView.root.y
            val childPos = recycler.getChildAt(index).y
            val y: Float = childPos - recyclerPos
            binding.scrollView.smoothScrollTo(0, y.toInt(), SCROLL_DURATION)
        }
    }

    companion object {
        private const val TAG = "FastLyricsFragment"
        private const val SCROLL_DURATION = 1000
    }
}