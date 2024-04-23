package io.github.teccheck.fastlyrics

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate

class Settings(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
    }

    fun getAppTheme(): Int {
        return sharedPreferences.getString(KEY_APP_THEME, DEFAULT_APP_THEME)?.toInt()
            ?: return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    @StyleRes
    fun getMaterialStyle(): Int {
        return when (sharedPreferences.getString(KEY_MATERIAL_STYLE, DEFAULT_MATERIAL_STYLE)) {
            MATERIAL_STYLE_ONE -> R.style.Theme_FastLyrics_Material1
            MATERIAL_STYLE_TWO -> R.style.Theme_FastLyrics_Material2
            MATERIAL_STYLE_THREE -> R.style.Theme_FastLyrics_Material3
            else -> R.style.Theme_FastLyrics_Material2
        }
    }

    fun getIsAutoRefreshEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_REFRESH, false)
    }

    fun getTextSize(): Int {
        return sharedPreferences.getInt(KEY_TEXT_SIZE, 18)
    }

    companion object {
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_MATERIAL_STYLE = "material_style"
        private const val KEY_AUTO_REFRESH = "auto_refresh"
        private const val KEY_TEXT_SIZE = "text_size"

        private const val MATERIAL_STYLE_ONE = "1"
        private const val MATERIAL_STYLE_TWO = "2"
        private const val MATERIAL_STYLE_THREE = "3"

        private const val DEFAULT_APP_THEME = "-1"
        private const val DEFAULT_MATERIAL_STYLE = MATERIAL_STYLE_TWO
    }

}