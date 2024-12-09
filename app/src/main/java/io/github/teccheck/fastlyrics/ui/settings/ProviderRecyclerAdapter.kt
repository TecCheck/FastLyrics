package io.github.teccheck.fastlyrics.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.utils.ProviderOrder
import io.github.teccheck.fastlyrics.utils.Utils

class ProviderRecyclerAdapter() :
    RecyclerView.Adapter<ProviderRecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val text: TextView = view.findViewById(R.id.text)
        private val switch: SwitchCompat = view.findViewById(R.id.provider_switch)

        private var provider: LyricsProvider? = null

        fun bind(provider: LyricsProvider) {
            this.provider = provider

            icon.setImageResource(Utils.getProviderIconRes(provider))
            text.setText(Utils.getProviderNameRes(provider))
            switch.isChecked = ProviderOrder.getEnabled(provider)
            switch.setOnCheckedChangeListener { _, enabled ->
                ProviderOrder.setEnabled(
                    provider,
                    enabled
                )
            }
        }
    }

    fun interface ToggleListener {
        fun toggle(lyricsProvider: LyricsProvider, enabled: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_provider_setting, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ProviderOrder.getOrderCount()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ProviderOrder.getProvider(position))
    }
}