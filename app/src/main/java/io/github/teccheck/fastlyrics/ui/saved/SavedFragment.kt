package io.github.teccheck.fastlyrics.ui.saved

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.databinding.FragmentSavedBinding
import io.github.teccheck.fastlyrics.ui.viewlyrics.ViewLyricsFragment

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var viewModel: SavedViewModel
    private lateinit var adapter: RecyclerAdapter
    private lateinit var selectionTracker: SelectionTracker<Long>

    private var actionMode: ActionMode? = null

    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            super.onSelectionChanged()

            if (selectionTracker.hasSelection()) {
                if (actionMode == null) {
                    actionMode =
                        (activity as AppCompatActivity).startSupportActionMode(actionModeCallback)
                }

                actionMode?.title =
                    getString(R.string.items_selected, selectionTracker.selection.size())
            } else {
                actionMode?.finish()
            }
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            activity?.menuInflater?.inflate(R.menu.fragment_saved_contextual_appbar_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete -> {
                    deleteItems(selectionTracker.selection.toList())
                    viewModel.fetchSongs()
                    actionMode?.finish()
                    selectionTracker.clearSelection()
                    true
                }

                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            selectionTracker.clearSelection()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SavedViewModel::class.java]
        _binding = FragmentSavedBinding.inflate(inflater, container, false)

        adapter = RecyclerAdapter()
        adapter.setHasStableIds(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        selectionTracker = SelectionTracker.Builder(
            "my-selection-id",
            binding.recyclerView,
            StableIdKeyProvider(binding.recyclerView),
            DetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()
        ).withOnItemActivatedListener(this::onItemActivated).build()

        selectionTracker.addObserver(selectionObserver)
        adapter.setSelectionTracker(selectionTracker)

        viewModel.songs.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "test")
            if (result is Success) {
                adapter.setSongs(result.value)
            }
        }

        viewModel.fetchSongs()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectionTracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        selectionTracker.onRestoreInstanceState(savedInstanceState)
    }

    private fun onItemActivated(
        item: ItemDetailsLookup.ItemDetails<Long>, e: MotionEvent
    ): Boolean {
        (item as DetailsLookup.SongWithLyricsDetails).songId?.let { viewSong(it) }
        return false
    }

    private fun viewSong(id: Long) {
        Log.d(TAG, "Show song $id")
        val bundle = Bundle()
        bundle.putLong(ViewLyricsFragment.ARG_SONG_ID, id)
        findNavController().navigate(R.id.nav_view_lyrics, bundle)
    }

    private fun deleteItems(itemIds: List<Long>) {
        val songIds =
            itemIds.map { (binding.recyclerView.findViewHolderForItemId(it) as RecyclerAdapter.ViewHolder).getSongId()!! }

        LyricStorage.deleteAsync(songIds)
    }

    companion object {
        private const val TAG = "SavedFragment"
    }
}