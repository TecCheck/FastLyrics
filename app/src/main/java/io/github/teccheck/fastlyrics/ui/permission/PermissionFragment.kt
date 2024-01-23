package io.github.teccheck.fastlyrics.ui.permission

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.databinding.FragmentPermissionBinding

class PermissionFragment : Fragment() {

    private var _binding: FragmentPermissionBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionBinding.inflate(inflater, container, false)
        binding.gotoSettingsButton.setOnClickListener { startNotificationsSettings() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        MediaSession.init(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startNotificationsSettings() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)
    }

    companion object {
        private const val TAG = "PermissionFragment"
    }
}