package io.github.teccheck.fastlyrics.ui.viewlyrics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.BaseActivity
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.databinding.ActivityViewLyricsBinding
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.model.SyncedLyrics
import io.github.teccheck.fastlyrics.utils.PlaceholderDrawable
import io.github.teccheck.fastlyrics.utils.Utils
import io.github.teccheck.fastlyrics.utils.Utils.getLyrics

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
        binding.refresher.setColorSchemeResources(
            R.color.theme_primary, R.color.theme_secondary
        )

        lyricsViewModel = ViewModelProvider(this)[ViewLyricsViewModel::class.java]

        lyricsViewModel.songWithLyrics.observe(this) { result ->
            binding.refresher.isRefreshing = false
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
        displayLyrics(song)

        val picasso = Picasso.get().load(song.artUrl).centerInside()

        val provider = LyricsProvider.getProviderByName(song.provider)
        provider?.let {
            val providerIconRes = Utils.getProviderIconRes(it)
            picasso.placeholder(PlaceholderDrawable(this, providerIconRes))

            val icon = AppCompatResources.getDrawable(this, providerIconRes)
            binding.lyricsView.source.setIconResource(providerIconRes)
            binding.lyricsView.textLyricsProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon, null, null, null
            )

            val nameRes = Utils.getProviderNameRes(it)
            val name = getString(nameRes)
            binding.lyricsView.source.text = name
            binding.lyricsView.textLyricsProvider.text = name
        }

        picasso.into(binding.header.imageSongArt)

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
        binding.lyricsView.textLyrics.text = if (song.type == LyricsType.LRC) {
            SyncedLyrics.parseLrcToList(song.lyricsSynced ?: "")
                .joinToString(separator = "\n") { it.text }
        } else {
            song.lyricsPlain
        }
    }

    private fun getSearchResult(intent: Intent): SearchResult? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(ARG_SEARCH_RESULT, SearchResult::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra(ARG_SEARCH_RESULT) as SearchResult
        }
    }

    private fun copyToClipboard(title: String, text: String) {
        val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
        val clip = ClipData.newPlainText(title, text)
        clipboard?.setPrimaryClip(clip)
    }

    private fun openLink(link: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

    private fun share(songTitle: String, artist: String, text: String) {
        val title = getString(R.string.share_title, songTitle, artist)
        ShareCompat.IntentBuilder(this).setText(text).setType("text/plain").setChooserTitle(title)
            .setSubject(title).startChooser()
    }

    companion object {
        private const val TAG = "ViewLyricsFragment"
        const val ARG_SONG_ID = "song_id"
        const val ARG_SEARCH_RESULT = "search_result"
    }
}