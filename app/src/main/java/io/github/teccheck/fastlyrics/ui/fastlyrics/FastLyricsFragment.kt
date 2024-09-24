package io.github.teccheck.fastlyrics.ui.fastlyrics

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.model.SyncedLyrics
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService
import io.github.teccheck.fastlyrics.utils.Utils
import io.github.teccheck.fastlyrics.utils.Utils.copyToClipboard
import io.github.teccheck.fastlyrics.utils.Utils.getLyrics
import io.github.teccheck.fastlyrics.utils.Utils.openLink
import io.github.teccheck.fastlyrics.utils.Utils.share


class FastLyricsFragment : Fragment() {

    private lateinit var lyricsViewModel: FastLyricsViewModel
    private var _binding: FragmentFastLyricsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        lyricsViewModel = ViewModelProvider(this)[FastLyricsViewModel::class.java]
        _binding = FragmentFastLyricsBinding.inflate(inflater, container, false)

        val context = requireContext()
        settings = Settings(context)

        binding.lyricsView.container.visibility = View.GONE

        lyricsViewModel.songMeta.observe(viewLifecycleOwner, this::songMetaObserver)
        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner, this::observer)
        lyricsViewModel.songWithLyricsSynced.observe(viewLifecycleOwner, this::observer)
        lyricsViewModel.songPosition.observe(viewLifecycleOwner, this::setTime)

        binding.refresher.setOnRefreshListener { loadLyricsForCurrentSong() }
        binding.refresher.setColorSchemeResources(R.color.theme_primary, R.color.theme_secondary)

        binding.header.syncedLyricsSwitch.isChecked = settings.getSyncedLyricsByDefault()
        binding.header.syncedLyricsSwitch.setOnCheckedChangeListener { _, _ -> displaySongWithLyrics() }

        if (DummyNotificationListenerService.canAccessNotifications(context)) {
            loadLyricsForCurrentSong()
            lyricsViewModel.setupSongMetaListener()
            lyricsViewModel.autoRefresh = Settings(context).getIsAutoRefreshEnabled()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val textSize = settings.getTextSize().toFloat()
        val textSizeFocusAdd = 2f

        binding.lyricsView.lyricViewX.apply {
            setNormalTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, resources.displayMetrics))
            setCurrentColor(resources.getColor(R.color.theme_primary))
            setCurrentTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize + textSizeFocusAdd, resources.displayMetrics))
        }

        binding.lyricsView.textLyrics.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        }
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
        binding.lyricsView.lyricViewX.updateTime(time)
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

        if (song.lyricsSynced != null) lyricsViewModel.syncedLyricsAvailable = true

        binding.header.syncedLyricsAvailable.visibility = if (lyricsViewModel.syncedLyricsAvailable) {
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
                getString(R.string.lyrics_clipboard_label), song.getLyrics()
            )
        }
        binding.lyricsView.share.setOnClickListener {
            share(
                song.title, song.artist, song.getLyrics()
            )
        }
    }

    private fun displayLyrics(song: SongWithLyrics) {
        binding.lyricsView.lyricViewX.visibility = View.GONE
        binding.lyricsView.textLyrics.visibility = View.GONE

        if (song.lyricsSynced != null && binding.header.syncedLyricsSwitch.isChecked) {
            binding.lyricsView.lyricViewX.visibility = View.VISIBLE
            binding.lyricsView.lyricViewX.loadLyric(SyncedLyrics.parseLrcToList(song.lyricsSynced))
            binding.lyricsView.textLyrics.text = null
            lyricsViewModel.setupPositionPolling(true)
        } else {
            binding.lyricsView.textLyrics.visibility = View.VISIBLE
            binding.lyricsView.textLyrics.text = song.lyricsPlain
            binding.lyricsView.lyricViewX.loadLyric(null)
            lyricsViewModel.setupPositionPolling(false)
        }
    }

    private fun getCurrentSongWithLyrics(): Result<SongWithLyrics, LyricsApiException>? {
        if (lyricsViewModel.syncedLyricsAvailable && binding.header.syncedLyricsSwitch.isChecked)
            return lyricsViewModel.songWithLyricsSynced.value

        return lyricsViewModel.songWithLyrics.value
    }

    companion object {
        private const val TAG = "FastLyricsFragment"
    }
}