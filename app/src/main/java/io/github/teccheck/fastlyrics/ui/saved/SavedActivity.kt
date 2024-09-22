package io.github.teccheck.fastlyrics.ui.saved

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.BaseActivity
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.databinding.ActivitySavedBinding
import io.github.teccheck.fastlyrics.ui.viewlyrics.ViewLyricsActivity

class SavedActivity : BaseActivity() {

    private lateinit var binding: ActivitySavedBinding
    private lateinit var viewModel: SavedViewModel
    private lateinit var adapter: RecyclerAdapter
    private lateinit var selectionTracker: SelectionTracker<Long>

    private var actionMode: ActionMode? = null

    // These two only forward to methods within SavedFragment
    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            super.onSelectionChanged()
            this@SavedActivity.onSelectionChanged()
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return this@SavedActivity.onCreateActionMode(menu)
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return this@SavedActivity.onActionItemClicked(mode, item)
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            this@SavedActivity.onDestroyActionMode()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbarLayout.toolbar, R.string.menu_saved)

        viewModel = ViewModelProvider(this)[SavedViewModel::class.java]

        adapter = RecyclerAdapter()
        adapter.setHasStableIds(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        selectionTracker = SelectionTracker.Builder(
            SELECTION_ID,
            binding.recyclerView,
            StableIdKeyProvider(binding.recyclerView),
            DetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()
        ).withOnItemActivatedListener(this::onItemActivated).build()

        selectionTracker.addObserver(selectionObserver)
        adapter.setSelectionTracker(selectionTracker)

        viewModel.songs.observe(this) { result ->
            if (result is Success) {
                adapter.setSongs(result.value)
            }
        }

        viewModel.fetchSongs()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectionTracker.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selectionTracker.onRestoreInstanceState(savedInstanceState)
    }

    private fun onItemActivated(
        item: ItemDetailsLookup.ItemDetails<Long>, e: MotionEvent
    ): Boolean {
        (item as DetailsLookup.SongWithLyricsDetails).songId?.let { viewSong(it) }
        return false
    }

    private fun onSelectionChanged() {
        if (!selectionTracker.hasSelection()) {
            actionMode?.finish()
            return
        }

        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback)
        }

        val count = selectionTracker.selection.size()
        actionMode?.title = resources.getQuantityString(R.plurals.items_selected, count, count)
    }

    private fun onCreateActionMode(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.fragment_saved_contextual_appbar_menu, menu)
        return true
    }

    private fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.delete -> {
                deleteItems(selectionTracker.selection.toList())
                viewModel.fetchSongs()
                selectionTracker.clearSelection()
                mode?.finish()
                true
            }

            else -> false
        }
    }

    private fun onDestroyActionMode() {
        actionMode = null
        selectionTracker.clearSelection()
    }

    private fun viewSong(id: Long) {
        val intent = Intent(this, ViewLyricsActivity::class.java)
        intent.putExtra(ViewLyricsActivity.ARG_SONG_ID, id)
        startActivity(intent)
    }

    private fun deleteItems(itemIds: List<Long>) {
        val songIds =
            itemIds.map { (binding.recyclerView.findViewHolderForItemId(it) as RecyclerAdapter.ViewHolder).getSongId()!! }

        LyricStorage.deleteAsync(songIds)
    }

    companion object {
        private const val TAG = "SavedFragment"
        private const val SELECTION_ID = "song-selection"
    }
}