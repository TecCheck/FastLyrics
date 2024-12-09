package io.github.teccheck.fastlyrics.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.fastlyrics.Settings
import io.github.teccheck.fastlyrics.databinding.FragmentProviderSettingsBinding
import io.github.teccheck.fastlyrics.utils.ProviderOrder

class ProviderSettingsFragment : Fragment() {

    private var _binding: FragmentProviderSettingsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var settings: Settings

    private val callback = object : SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val adapter = recyclerView.adapter
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition

            ProviderOrder.swap(from, to)
            adapter?.notifyItemMoved(from, to)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderSettingsBinding.inflate(inflater, container, false)
        settings = Settings(requireContext())

        binding.recycler.adapter = ProviderRecyclerAdapter()
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(binding.recycler)

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        ProviderOrder.save()
    }

    companion object {
        private val TAG = Companion::class.java.name
    }
}