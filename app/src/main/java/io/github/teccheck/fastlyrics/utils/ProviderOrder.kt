package io.github.teccheck.fastlyrics.utils

import android.content.Context
import android.content.SharedPreferences
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.utils.Utils.swap

object ProviderOrder {
    private const val KEY_PREFIX_PROVIDER_ORDER = "provider_order"
    private const val KEY_PREFIX_PROVIDER_ENABLED = "provider_enabled"

    private lateinit var sharedPreferences: SharedPreferences

    private var order = arrayOf<LyricsProvider>()
    private var enabled = mutableMapOf<LyricsProvider, Boolean>()

    val providers: Array<LyricsProvider> get() = order.filter { enabled[it] == true }.toTypedArray()

    fun init(context: Context) {
        sharedPreferences =
            context.getSharedPreferences(context.packageName + "_providers", Context.MODE_PRIVATE)
        load()
    }

    fun setEnabled(provider: LyricsProvider, enabled: Boolean) {
        this.enabled[provider] = enabled
    }

    fun getEnabled(provider: LyricsProvider): Boolean {
        return this.enabled[provider] ?: false
    }

    fun getOrderCount(): Int = order.size

    fun swap(first: Int, second: Int) {
        order.swap(first, second)
    }

    fun getProvider(index: Int): LyricsProvider = order[index]

    fun load() {
        this.order = LyricsProvider.getAllProviders().associateWith {
            sharedPreferences.getInt(orderKey(it.getName()), -1)
        }.toList().sortedBy { it.second }.map { it.first }.toTypedArray()

        this.enabled = LyricsProvider.getAllProviders().associateWith {
            sharedPreferences.getBoolean(enabledKey(it.getName()), true)
        }.toMutableMap()
    }

    fun save() {
        val editor = sharedPreferences.edit()
        this.order.forEachIndexed { index, lyricsProvider ->
            editor.putInt(
                orderKey(lyricsProvider.getName()), index
            )
        }
        this.enabled.forEach { editor.putBoolean(enabledKey(it.key.getName()), it.value) }
        editor.apply()
    }

    private fun orderKey(name: String) = "${KEY_PREFIX_PROVIDER_ORDER}_${name}"
    private fun enabledKey(name: String) = "${KEY_PREFIX_PROVIDER_ENABLED}_${name}"
}