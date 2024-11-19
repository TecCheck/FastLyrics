package io.github.teccheck.fastlyrics.ui.viewlyrics

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.BaseActivity
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.databinding.ActivityViewLyricsBinding
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.utils.PlaceholderDrawable
import io.github.teccheck.fastlyrics.utils.Utils
import io.github.teccheck.fastlyrics.utils.Utils.copyToClipboard
import io.github.teccheck.fastlyrics.utils.Utils.getLyrics
import io.github.teccheck.fastlyrics.utils.Utils.openLink
import io.github.teccheck.fastlyrics.utils.Utils.share

class ViewLyricsActivity : BaseActivity() {

    private lateinit var binding: ActivityViewLyricsBinding
    private lateinit var lyricsViewModel: ViewLyricsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewLyricsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbarLayout.toolbar)

        binding.lyricsView.lyricViewX.visibility = View.GONE
        binding.refresher.isEnabled = false
        binding.refresher.setColorSchemeResources(R.color.theme_primary, R.color.theme_secondary)

        lyricsViewModel = ViewModelProvider(this)[ViewLyricsViewModel::class.java]

        lyricsViewModel.songWithLyrics.observe(this) { result ->
            if (result is Success) displaySongWithLyrics(result.value)
        }

        if (intent.hasExtra(ARG_SONG_ID)) {
            lyricsViewModel.loadLyricsForSongFromStorage(intent.getLongExtra(ARG_SONG_ID, 0))
            return
        }

        if (intent.hasExtra(ARG_SEARCH_RESULT)) {
            binding.refresher.isRefreshing = true
            val result = getSearchResult(intent) ?: return
            lyricsViewModel.loadLyricsForSearchResult(result)
        }
    }

    private fun displaySongWithLyrics(song: SongWithLyrics) {
        binding.header.textSongTitle.text = song.title
        binding.header.textSongArtist.text = song.artist
        binding.lyricsView.textLyrics.text = song.getLyrics()

        val picasso = Picasso.get().load(song.artUrl)

        LyricsProvider.getProviderByName(song.provider)?.let {
            val nameRes = Utils.getProviderNameRes(it)
            val providerIconRes = Utils.getProviderIconRes(it)

            picasso.placeholder(PlaceholderDrawable(this, providerIconRes))

            binding.lyricsView.source.setText(nameRes)
            binding.lyricsView.source.setIconResource(providerIconRes)

            binding.lyricsView.textLyricsProvider.setText(nameRes)
            binding.lyricsView.textLyricsProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(
                providerIconRes, 0, 0, 0
            )
        }

        picasso.into(binding.header.imageSongArt)

        binding.lyricsView.source.setOnClickListener {
            openLink(this@ViewLyricsActivity, song.sourceUrl)
        }
        binding.lyricsView.copy.setOnClickListener {
            copyToClipboard(
                this@ViewLyricsActivity,
                getString(R.string.lyrics_clipboard_label),
                song.getLyrics()
            )
        }
        binding.lyricsView.share.setOnClickListener {
            share(this@ViewLyricsActivity, song.title, song.artist, song.getLyrics())
        }
    }

    private fun getSearchResult(intent: Intent): SearchResult? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(ARG_SEARCH_RESULT, SearchResult::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra(ARG_SEARCH_RESULT) as SearchResult
        }
    }

    companion object {
        private const val TAG = "ViewLyricsFragment"
        const val ARG_SONG_ID = "song_id"
        const val ARG_SEARCH_RESULT = "search_result"
    }
}