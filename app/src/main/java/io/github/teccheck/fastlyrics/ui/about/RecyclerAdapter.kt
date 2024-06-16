package io.github.teccheck.fastlyrics.ui.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.utils.Utils

class RecyclerAdapter(private val urlOpener: UrlOpener) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val text: TextView = view.findViewById(R.id.text)

        fun bind(provider: LyricsProvider, linkOpener: UrlOpener) {
            icon.setImageResource(Utils.getProviderIconRes(provider))
            text.setText(Utils.getProviderNameRes(provider))
            itemView.setOnClickListener { linkOpener.openUrl(Utils.getProviderUrlRes(provider)) }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_provider, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(LyricsProvider.getAllProviders()[position], urlOpener)
    }

    override fun getItemCount() = LyricsProvider.getAllProviders().size

    fun interface UrlOpener {
        fun openUrl(@StringRes linkRes: Int?)
    }
}