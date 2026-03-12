package com.mylibrary.app.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mylibrary.app.MainActivity
import com.mylibrary.app.data.LibraryManager
import com.mylibrary.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@registerForActivityResult
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                out.write(LibraryManager.exportJson().toByteArray())
            }
            Toast.makeText(requireContext(), "Backup exported", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        try {
            val json = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return@registerForActivityResult
            val count = LibraryManager.importJson(json)
            Toast.makeText(requireContext(), "Imported $count books", Toast.LENGTH_SHORT).show()
            updateStats()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Import failed — invalid file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { (activity as MainActivity).goBack() }

        binding.btnExport.setOnClickListener {
            exportLauncher.launch("MyLibrary-backup.json")
        }

        binding.btnImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        binding.btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear all data?")
                .setMessage("All books and summaries will be permanently deleted.")
                .setPositiveButton("Clear") { _, _ ->
                    LibraryManager.clearAll()
                    updateStats()
                    Toast.makeText(requireContext(), "Library cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        updateStats()
    }

    private fun updateStats() {
        val books = LibraryManager.getBooks()
        val totalNotes = books.sumOf { it.chapters.size }
        binding.tvStats.text = "${books.size} book${if (books.size != 1) "s" else ""} · $totalNotes chapter summar${if (totalNotes != 1) "ies" else "y"}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
