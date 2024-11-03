package io.github.teccheck.fastlyrics.utils

import android.content.Context
import android.graphics.drawable.LayerDrawable
import androidx.annotation.DrawableRes
import io.github.teccheck.fastlyrics.R

class PlaceholderDrawable(context: Context, @DrawableRes drawable: Int) :
    LayerDrawable(
        arrayOf(
            context.resources.getDrawable(R.color.art_placeholder_background),
            context.getDrawable(drawable)?.apply { setTint(context.resources.getColor(R.color.art_placeholder_foreground)) }
        )
    ) {

    init {
        val padding = context.resources.getDimension(R.dimen.placeholder_drawable_padding).toInt()
        this.setLayerInset(1, padding, padding, padding, padding)
    }
}