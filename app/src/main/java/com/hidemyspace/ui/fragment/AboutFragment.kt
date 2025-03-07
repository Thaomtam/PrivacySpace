package com.hidemyspace.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hidemyspace.BuildConfig
import com.hidemyspace.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupVersionInfo()
        setupLinks()
    }
    
    private fun setupVersionInfo() {
        binding.versionText.text = "Phiên bản: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }
    
    private fun setupLinks() {
        binding.githubButton.setOnClickListener {
            openUrl("https://github.com/yourusername/HideMySpace")
        }
        
        binding.telegramButton.setOnClickListener {
            openUrl("https://t.me/hidemyspace")
        }
        
        binding.donateButton.setOnClickListener {
            openUrl("https://paypal.me/yourusername")
        }
    }
    
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 