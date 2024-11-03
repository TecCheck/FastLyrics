package io.github.teccheck.fastlyrics.ui.fastlyrics

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.valueOrNull
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.Settings
import io.github.teccheck.fastlyrics.databinding.FragmentFastLyricsBinding
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.utils.Utils
import io.github.teccheck.fastlyrics.utils.Utils.copyToClipboard
import io.github.teccheck.fastlyrics.utils.Utils.openLink
import io.github.teccheck.fastlyrics.utils.Utils.setVisible
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

        settings = Settings(requireContext())

        lyricsViewModel.songMeta.observe(viewLifecycleOwner, this::songMetaObserver)
        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner, this::songWithLyricsObserver)
        lyricsViewModel.songPosition.observe(viewLifecycleOwner, this::setTime)

        binding.refresher.setOnRefreshListener { loadLyricsForCurrentSong() }
        binding.refresher.setColorSchemeResources(R.color.theme_primary, R.color.theme_secondary)

        binding.header.syncedLyricsSwitch.isChecked = settings.getSyncedLyricsByDefault()
        binding.header.syncedLyricsSwitch.setOnCheckedChangeListener { _, checked ->
            showSynced(checked)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        lyricsViewModel.setupSongMetaListener()
        setNewState(lyricsViewModel.state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setNewState(state: UiState) {
        lyricsViewModel.state = state

        binding.refresher.isRefreshing = state.isRefreshing

        // Header
        binding.header.root.setVisible(state.showHeader)
        binding.header.textSongTitle.text = state.getSongTitle()
        binding.header.textSongArtist.text = state.getSongArtist()
        binding.header.syncedLyricsAvailable.setVisible(state.hasSyncedLyrics())

        Picasso.get().load(state.getArtUrl())
            .placeholder(BitmapDrawable(resources, state.getArtBitmap()))
            .into(binding.header.imageSongArt)

        // Error
        binding.errorView.root.setVisible(state.showError)
        state.getErrorText()?.let { binding.errorView.errorText.setText(it) }
        state.getErrorIcon()?.let { binding.errorView.errorIcon.setImageResource(it) }

        // Lyrics
        binding.lyricsView.root.setVisible(state.showText)
        binding.lyricsView.textLyrics.text = state.getLyrics()
        state.getSyncedLyrics()?.let { binding.lyricsView.lyricViewX.loadLyric(it) }

        state.getSongProvider()?.let {
            val providerIconRes = Utils.getProviderIconRes(it)
            val providerNameRes = Utils.getProviderNameRes(it)

            binding.lyricsView.source.setText(providerNameRes)
            binding.lyricsView.source.setIconResource(providerIconRes)

            binding.lyricsView.textLyricsProvider.setText(providerNameRes)
            binding.lyricsView.textLyricsProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(
                providerIconRes, 0, 0, 0
            )
        }

        binding.lyricsView.source.setOnClickListener { openLink(state.getSourceUrl()) }
        binding.lyricsView.copy.setOnClickListener {
            copyToClipboard(getString(R.string.lyrics_clipboard_label), state.getLyrics())
        }
        binding.lyricsView.share.setOnClickListener {
            share(state.getSongTitle(), state.getSongArtist(), state.getLyrics())
        }

        showSynced(binding.header.syncedLyricsSwitch.isChecked)

        if (state.startRefresh) loadLyricsForCurrentSong()


        // Apply settings
        lyricsViewModel.autoRefresh = settings.getIsAutoRefreshEnabled()

        val textSize = settings.getTextSize().toFloat()
        val textSizeFocusAdd = 2f

        binding.lyricsView.lyricViewX.apply {
            setNormalTextSize(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, textSize, resources.displayMetrics
                )
            )
            setCurrentColor(resources.getColor(R.color.theme_primary))
            setCurrentTextSize(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    textSize + textSizeFocusAdd,
                    resources.displayMetrics
                )
            )
        }

        binding.lyricsView.textLyrics.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
    }

    private fun loadLyricsForCurrentSong() {
        binding.refresher.isRefreshing = true
        val success = lyricsViewModel.loadLyricsForCurrentSong(requireContext())
        if (!success) binding.refresher.isRefreshing = false
    }

    private fun songMetaObserver(result: Result<SongMeta, LyricsApiException>) {
        when (result) {
            is Failure -> setNewState(NoMusicState())
            is Success -> setNewState(LoadingState(result.value))
        }
    }

    private fun songWithLyricsObserver(result: Result<SongWithLyrics, LyricsApiException>) {
        when (result) {
            is Failure -> setNewState(
                ErrorState(lyricsViewModel.songMeta.value?.valueOrNull(), result.reason)
            )

            is Success -> setNewState(
                TextState(lyricsViewModel.songMeta.value?.valueOrNull(), result.value)
            )
        }
    }

    private fun showSynced(show: Boolean) {
        val synced = show && lyricsViewModel.state.hasSyncedLyrics()

        binding.lyricsView.textLyrics.setVisible(!synced)
        binding.lyricsView.lyricViewX.setVisible(synced)
        lyricsViewModel.setupPositionPolling(synced)
    }

    private fun setTime(time: Long) {
        binding.lyricsView.lyricViewX.updateTime(time)
    }

    companion object {
        private const val TAG = "FastLyricsFragment"
    }
}