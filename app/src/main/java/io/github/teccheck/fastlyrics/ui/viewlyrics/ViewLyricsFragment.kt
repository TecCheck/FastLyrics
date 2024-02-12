package io.github.teccheck.fastlyrics.ui.viewlyrics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.databinding.FragmentViewLyricsBinding
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.model.SyncedLyrics
import io.github.teccheck.fastlyrics.ui.fastlyrics.FastLyricsFragment
import io.github.teccheck.fastlyrics.utils.Utils
import io.github.teccheck.fastlyrics.utils.Utils.copyToClipboard
import io.github.teccheck.fastlyrics.utils.Utils.openLink
import io.github.teccheck.fastlyrics.utils.Utils.share
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ViewLyricsFragment : Fragment() {

    private lateinit var lyricsViewModel: ViewLyricsViewModel
    private var _binding: FragmentViewLyricsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        lyricsViewModel = ViewModelProvider(this)[ViewLyricsViewModel::class.java]
        _binding = FragmentViewLyricsBinding.inflate(inflater, container, false)

        binding.refresher.isEnabled = false
        binding.refresher.setColorSchemeResources(
            R.color.theme_primary, R.color.theme_secondary
        )

        lyricsViewModel.songWithLyrics.observe(viewLifecycleOwner) { result ->
            binding.refresher.isRefreshing = false
            if (result is Success) displaySongWithLyrics(result.value)
        }

        arguments?.let {
            if (it.containsKey(ARG_SONG_ID)) {
                lyricsViewModel.loadLyricsForSongFromStorage(it.getLong(ARG_SONG_ID, 0))
            } else if (it.containsKey(ARG_SEARCH_RESULT)) {
                binding.refresher.isRefreshing = true
                val result = getSearchResult(it) ?: return@let
                lyricsViewModel.loadLyricsForSearchResult(result)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displaySongWithLyrics(song: SongWithLyrics) {
        binding.header.textSongTitle.text = song.title
        binding.header.textSongArtist.text = song.artist
        displayLyrics(song)
        Picasso.get().load(song.artUrl).into(binding.header.imageSongArt)

        val provider = LyricsProvider.getProviderByName(song.provider)
        provider?.let {
            val providerIconRes = Utils.getProviderIconRes(it)
            val icon = AppCompatResources.getDrawable(requireContext(), providerIconRes)
            binding.lyricsView.source.setIconResource(providerIconRes)
            binding.lyricsView.textLyricsProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)

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
                song.title,
                song.artist,
                song.lyrics
            )
        }
        binding.lyricsView.download.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(requireContext(), "Tap download again after allowing", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000) // Delay for 1 second
                    // After delay, request permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        ViewLyricsFragment.PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                // Permission is already granted, proceed with saving the lyrics to a file
                saveLyricsToFile(requireContext(), song.title, song.lyrics)
            }
        }
    }
    private fun saveLyricsToFile(context: Context, title: String, lyrics: String) {
        val externalStorageState = Environment.getExternalStorageState()
        if (externalStorageState == Environment.MEDIA_MOUNTED) {
            val file = File(
                Environment.getExternalStorageDirectory(),
                "$title.txt"
            )
            FileOutputStream(file).use { fos ->
                fos.write(lyrics.toByteArray())
            }
            Toast.makeText(context, "Lyrics saved to $title.txt", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "External storage not available", Toast.LENGTH_SHORT).show()
        }
    }


    private fun displayLyrics(song: SongWithLyrics) {
        binding.lyricsView.textLyrics.text = if (song.type == LyricsType.LRC) {
            val syncedLyrics = SyncedLyrics.parseLrc(song.lyrics)
            syncedLyrics?.getFullText() ?: ""
        } else {
            song.lyrics
        }
    }

    private fun getSearchResult(args: Bundle): SearchResult? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.getSerializable(ARG_SEARCH_RESULT, SearchResult::class.java)
        } else {
            @Suppress("DEPRECATION")
            args.getSerializable(ARG_SEARCH_RESULT) as SearchResult
        }
    }

    companion object {
        private const val TAG = "ViewLyricsFragment"
        const val ARG_SONG_ID = "song_id"
        const val ARG_SEARCH_RESULT = "search_result"
        const val PERMISSION_REQUEST_CODE = 1001
    }
}