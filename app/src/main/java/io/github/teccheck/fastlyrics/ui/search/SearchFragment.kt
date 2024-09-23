package io.github.teccheck.fastlyrics.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.MainActivity
import io.github.teccheck.fastlyrics.databinding.FragmentSearchBinding
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.ui.viewlyrics.ViewLyricsActivity

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val searchTimer = SearchTimer(this::onQueryTextSubmit)

    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var selectionTracker: SelectionTracker<Long>

    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        searchMenuItem = (activity as MainActivity).getSearchMenuItem()
        searchMenuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                findNavController().navigateUp()
                return true
            }
        })

        searchView = (searchMenuItem?.actionView as SearchView?)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return this@SearchFragment.onQueryTextSubmit(query)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return this@SearchFragment.onQueryTextChange(newText)
            }
        })

        recyclerAdapter = RecyclerAdapter()
        recyclerAdapter.setHasStableIds(true)
        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        selectionTracker = SelectionTracker.Builder(
            SELECTION_ID,
            binding.recyclerView,
            StableIdKeyProvider(binding.recyclerView),
            DetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()
        ).withOnItemActivatedListener(this::onItemActivated)
            .withSelectionPredicate(SelectionPredicate()).build()

        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            binding.progresIndicator.visibility = View.GONE
            if (searchResults is Success) recyclerAdapter.setSearchResults(searchResults.value)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchView?.setOnQueryTextListener(null)
        searchMenuItem?.setOnActionExpandListener(null)
    }

    private fun onQueryTextSubmit(query: String?): Boolean {
        if (query?.isBlank() != false)
            return true

        binding.progresIndicator.visibility = View.VISIBLE
        viewModel.search(query)
        return true
    }

    private fun onQueryTextChange(newText: String?): Boolean {
        searchTimer.setQuery(newText)
        return true
    }

    private fun onItemActivated(
        item: ItemDetailsLookup.ItemDetails<Long>, e: MotionEvent
    ): Boolean {
        val viewHolder = item.selectionKey?.let { binding.recyclerView.findViewHolderForItemId(it) }
        val searchResult = (viewHolder as RecyclerAdapter.ViewHolder).getSearchResult()
        searchResult?.let { viewSearchResult(it) }
        return false
    }

    private fun viewSearchResult(searchResult: SearchResult) {
        Log.d(TAG, "Show search result $searchResult")
        val intent = Intent(requireContext(), ViewLyricsActivity::class.java)
        intent.putExtra(ViewLyricsActivity.ARG_SEARCH_RESULT, searchResult)
        startActivity(intent)

        searchMenuItem?.setOnActionExpandListener(null)
        searchMenuItem?.collapseActionView()
    }

    companion object {
        private const val TAG = "SearchFragment"
        private const val SELECTION_ID = "search"
    }
}