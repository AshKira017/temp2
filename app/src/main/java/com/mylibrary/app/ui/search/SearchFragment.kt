package com.mylibrary.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mylibrary.app.MainActivity
import com.mylibrary.app.data.ApiService
import com.mylibrary.app.data.BookSearchResult
import com.mylibrary.app.data.LibraryManager
import com.mylibrary.app.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SearchAdapter(
            isAdded = { LibraryManager.hasBook(it.id) },
            onAdd = { result -> showCoverPicker(result) }
        )

        binding.recyclerResults.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerResults.adapter = adapter

        binding.btnBack.setOnClickListener { (activity as MainActivity).goBack() }

        binding.btnSearch.setOnClickListener { doSearch() }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); true } else false
        }

        binding.etSearch.post {
            binding.etSearch.requestFocus()
            requireContext().getSystemService<InputMethodManager>()
                ?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun doSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isBlank()) return

        hideKeyboard()
        binding.progressBar.visibility = View.VISIBLE
        binding.tvHint.visibility = View.GONE
        binding.recyclerResults.visibility = View.GONE

        lifecycleScope.launch {
            val results = ApiService.searchBooks(query)
            if (_binding == null) return@launch

            binding.progressBar.visibility = View.GONE
            if (results.isEmpty()) {
                binding.tvHint.visibility = View.VISIBLE
                binding.tvHint.text = "No results found. Try a different title."
            } else {
                binding.recyclerResults.visibility = View.VISIBLE
                adapter.submitList(results)
            }
        }
    }

    fun showCoverPicker(result: BookSearchResult) {
        CoverPickerSheet.newInstance(result) {
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Added to shelf", Toast.LENGTH_SHORT).show()
        }.show(childFragmentManager, "cover_picker")
    }

    private fun hideKeyboard() {
        requireContext().getSystemService<InputMethodManager>()
            ?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
