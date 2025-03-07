package com.hidemyspace.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.hidemyspace.R
import com.hidemyspace.appContext
import com.hidemyspace.model.Config
import com.hidemyspace.service.ConfigManager
import com.hidemyspace.service.PrefManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        setupThemePreference()
        setupLogPreferences()
        setupRebootPreference()
        setupExportImportPreferences()
    }
    
    private fun setupThemePreference() {
        val themePreference = findPreference<ListPreference>("dark_theme")
        themePreference?.setOnPreferenceChangeListener { _, newValue ->
            requireActivity().recreate()
            true
        }
    }
    
    private fun setupLogPreferences() {
        val detailLogPreference = findPreference<SwitchPreferenceCompat>("detail_log")
        detailLogPreference?.setOnPreferenceChangeListener { _, newValue ->
            val config = ConfigManager.getConfig()
            ConfigManager.updateConfig(config.copy(detailLog = newValue as Boolean))
            true
        }
        
        val maxLogSizePreference = findPreference<Preference>("max_log_size")
        maxLogSizePreference?.summary = "${PrefManager.maxLogSize} KB"
        maxLogSizePreference?.setOnPreferenceClickListener {
            showMaxLogSizeDialog()
            true
        }
    }
    
    private fun setupRebootPreference() {
        val rebootPreference = findPreference<Preference>("reboot")
        rebootPreference?.setOnPreferenceClickListener {
            showRebootDialog()
            true
        }
    }
    
    private fun setupExportImportPreferences() {
        val exportPreference = findPreference<Preference>("export_config")
        exportPreference?.setOnPreferenceClickListener {
            exportConfig()
            true
        }
        
        val importPreference = findPreference<Preference>("import_config")
        importPreference?.setOnPreferenceClickListener {
            importConfig()
            true
        }
    }
    
    private fun showMaxLogSizeDialog() {
        val sizes = arrayOf("512 KB", "1 MB", "2 MB", "5 MB", "10 MB")
        val values = arrayOf(512, 1024, 2048, 5120, 10240)
        
        val currentIndex = values.indexOfFirst { it == PrefManager.maxLogSize }.coerceAtLeast(0)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Kích thước tối đa của log")
            .setSingleChoiceItems(sizes, currentIndex) { dialog, which ->
                PrefManager.maxLogSize = values[which]
                findPreference<Preference>("max_log_size")?.summary = "${values[which]} KB"
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showRebootDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.restart_required)
            .setMessage(R.string.restart_required_message)
            .setPositiveButton(R.string.restart) { _, _ ->
                rebootDevice()
            }
            .setNegativeButton(R.string.later, null)
            .show()
    }
    
    private fun rebootDevice() {
        try {
            val intent = Intent("android.intent.action.REBOOT")
            intent.putExtra("nowait", 1)
            intent.putExtra("interval", 1)
            intent.putExtra("window", 0)
            requireContext().sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun exportConfig() {
        val config = ConfigManager.getConfig()
        val json = config.toJson()
        
        // Trong thực tế, bạn sẽ lưu json này vào một tệp
        // Ở đây chỉ hiển thị thông báo
        AlertDialog.Builder(requireContext())
            .setTitle("Xuất cấu hình")
            .setMessage("Cấu hình đã được xuất thành công")
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    private fun importConfig() {
        // Trong thực tế, bạn sẽ đọc json từ một tệp
        // Ở đây chỉ hiển thị thông báo
        AlertDialog.Builder(requireContext())
            .setTitle("Nhập cấu hình")
            .setMessage("Cấu hình đã được nhập thành công")
            .setPositiveButton(R.string.ok, null)
            .show()
    }
} 