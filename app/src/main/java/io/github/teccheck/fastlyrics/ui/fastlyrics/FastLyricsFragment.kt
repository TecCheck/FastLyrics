package io.github.teccheck.fastlyrics.ui.fastlyrics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
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
import io.github.teccheck.fastlyrics.exceptions.NoMusicPlayingException
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

        val context = requireContext()

        binding.lyricsView.container.visibility = View.GONE

        lyricsViewModel.songMeta.observe(viewLifecycleOwner, this::songMetaObserver)
        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner, this::observer)
        lyricsViewModel.songWithLyricsSynced.observe(viewLifecycleOwner, this::observer)
        lyricsViewModel.songPosition.observe(viewLifecycleOwner, this::setTime)

        binding.refresher.setOnRefreshListener { loadLyricsForCurrentSong() }
        binding.refresher.setColorSchemeResources(R.color.theme_primary, R.color.theme_secondary)

        binding.header.syncedLyricsSwitch.setOnCheckedChangeListener { _, _ -> displaySongWithLyrics() }

        if (DummyNotificationListenerService.canAccessNotifications(context)) {
            loadLyricsForCurrentSong()
            lyricsViewModel.setupSongMetaListener()
            lyricsViewModel.autoRefresh = Settings(context).getIsAutoRefreshEnabled()
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
        binding.refresher.isRefreshing = true
        val success = lyricsViewModel.loadLyricsForCurrentSong(requireContext())
        if (!success) binding.refresher.isRefreshing = false
    }

    private fun songMetaObserver(result: Result<SongMeta, LyricsApiException>) {
        binding.header.container.visibility = View.GONE
        binding.errorView.container.visibility = View.GONE
        binding.lyricsView.container.visibility = View.GONE

        when (result) {
            is Failure -> displayError(result.reason)
            is Success -> displaySongMeta(result.value)
        }
    }

    private fun observer(result: Result<SongWithLyrics, LyricsApiException>) {
        binding.header.container.visibility = View.GONE
        binding.errorView.container.visibility = View.GONE
        binding.lyricsView.container.visibility = View.GONE

        if (result is Success) handleSongWithLyrics(result.value)

        displaySongWithLyrics()
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

    private fun displayError(exception: LyricsApiException) {
        binding.refresher.isRefreshing = false

        binding.errorView.container.visibility = View.VISIBLE
        if (exception !is NoMusicPlayingException) binding.header.container.visibility =
            View.VISIBLE

        binding.errorView.errorText.setText(Utils.getErrorTextRes(exception))
        binding.errorView.errorIcon.setImageResource(Utils.getErrorIconRes(exception))
    }

    private fun displaySongMeta(songMeta: SongMeta) {
        binding.header.container.visibility = View.VISIBLE

        binding.header.textSongTitle.text = songMeta.title
        binding.header.textSongArtist.text = songMeta.artist
        binding.header.imageSongArt.setImageBitmap(songMeta.art)
    }

    private fun handleSongWithLyrics(song: SongWithLyrics) {
        Log.d(TAG, "Handling $song")

        binding.refresher.isRefreshing = false

        when (song.type) {
            LyricsType.LRC -> lyricsViewModel.syncedLyricsAvailable = true
            LyricsType.RAW_TEXT -> lyricsViewModel.plainLyricsAvailable = true
        }

        binding.header.syncedLyricsAvailable.visibility = if (lyricsViewModel.bothLyricsAvailable) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun displaySongWithLyrics() {
        val result = getCurrentSongWithLyrics() ?: return

        if (result is Failure) {
            displayError(result.reason)
            return
        }

        val song = (result as Success).value

        binding.header.container.visibility = View.VISIBLE
        binding.lyricsView.container.visibility = View.VISIBLE

        binding.header.textSongTitle.text = song.title
        binding.header.textSongArtist.text = song.artist

        displayLyrics(song)
        Picasso.get().load(song.artUrl).into(binding.header.imageSongArt)

        LyricsProvider.getProviderByName(song.provider)?.let {
            val providerIconRes = Utils.getProviderIconRes(it)
            val icon = AppCompatResources.getDrawable(requireContext(), providerIconRes)
            binding.lyricsView.source.setIconResource(providerIconRes)
            binding.lyricsView.textLyricsProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon, null, null, null
            )

            val nameRes = Utils.getProviderNameRes(it)
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
        binding.lyricsView.syncedRecycler.visibility = View.GONE
        binding.lyricsView.textLyrics.visibility = View.GONE

        if (song.type == LyricsType.RAW_TEXT) {
            binding.lyricsView.textLyrics.visibility = View.VISIBLE
            binding.lyricsView.textLyrics.text = song.lyrics
            recyclerAdapter.setSyncedLyrics(null)
            lyricsViewModel.setupPositionPolling(false)
        } else if (song.type == LyricsType.LRC) {
            binding.lyricsView.syncedRecycler.visibility = View.VISIBLE
            SyncedLyrics.parseLrc(song.lyrics)?.let { recyclerAdapter.setSyncedLyrics(it) }
            binding.lyricsView.textLyrics.text = ""
            lyricsViewModel.setupPositionPolling(true)
        }
    }

    private fun getCurrentSongWithLyrics(): Result<SongWithLyrics, LyricsApiException>? {
        val result = if (lyricsViewModel.bothLyricsAvailable) {
            if (binding.header.syncedLyricsSwitch.isChecked) {
                lyricsViewModel.songWithLyricsSynced.value
            } else {
                lyricsViewModel.songWithLyrics.value
            }
        } else if (lyricsViewModel.syncedLyricsAvailable) {
            lyricsViewModel.songWithLyricsSynced.value
        } else {
            lyricsViewModel.songWithLyrics.value
        }

        return result
    }

    companion object {
        private const val TAG = "FastLyricsFragment"
        private const val SCROLL_DURATION = 1000
    }
}