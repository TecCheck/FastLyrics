package io.github.teccheck.fastlyrics.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.teccheck.fastlyrics.BuildConfig
import io.github.teccheck.fastlyrics.BaseActivity
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbarLayout.toolbar, R.string.menu_about)

        binding.textVersion.text = BuildConfig.VERSION_NAME
        binding.layoutSourceCode.setOnClickListener { openUrl(R.string.source_code_url) }
        binding.recycler.adapter = RecyclerAdapter(this::openUrl)
        binding.recycler.layoutManager = LinearLayoutManager(this)
    }

    private fun openUrl(@StringRes urlRes: Int?) {
        if (urlRes == null) return
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(urlRes))))
    }
}