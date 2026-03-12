package com.mylibrary.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mylibrary.app.data.ApiService
import com.mylibrary.app.data.Book
import com.mylibrary.app.data.BookSearchResult
import com.mylibrary.app.data.LibraryManager
import com.mylibrary.app.databinding.FragmentCoverPickerBinding
import kotlinx.coroutines.launch

class CoverPickerSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentCoverPickerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CoverAdapter
    private var selectedUrl: String? = null
    private var onAdded: (() -> Unit)? = null

    private val result: BookSearchResult by lazy {
        BookSearchResult(
            id = requireArguments().getString(ARG_ID)!!,
            title = requireArguments().getString(ARG_TITLE)!!,
            author = requireArguments().getString(ARG_AUTHOR)!!,
            coverUrl = requireArguments().getString(ARG_COVER)!!
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCoverPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = result.title
        binding.tvAuthor.text = result.author

        adapter = CoverAdapter { url ->
            selectedUrl = url
        }

        binding.recyclerCovers.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerCovers.adapter = adapter

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnAdd.setOnClickListener {
            val book = Book(
                id = result.id,
                title = result.title,
                author = result.author,
                coverUrl = selectedUrl ?: result.coverUrl
            )
            LibraryManager.addBook(book)
            onAdded?.invoke()
            dismiss()
        }

        fetchCovers()
    }

    private fun fetchCovers() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerCovers.visibility = View.GONE
        binding.tvNoCover.visibility = View.GONE

        lifecycleScope.launch {
            val covers = ApiService.fetchCovers(result.title, result.author)
            if (_binding == null) return@launch

            binding.progressBar.visibility = View.GONE
            if (covers.isEmpty()) {
                binding.tvNoCover.visibility = View.VISIBLE
                selectedUrl = result.coverUrl
            } else {
                binding.recyclerCovers.visibility = View.VISIBLE
                selectedUrl = covers.first().url
                adapter.submitList(covers)
                adapter.setSelected(covers.first().url)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_TITLE = "title"
        private const val ARG_AUTHOR = "author"
        private const val ARG_COVER = "cover"

        fun newInstance(result: BookSearchResult, onAdded: () -> Unit): CoverPickerSheet {
            return CoverPickerSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, result.id)
                    putString(ARG_TITLE, result.title)
                    putString(ARG_AUTHOR, result.author)
                    putString(ARG_COVER, result.coverUrl)
                }
                this.onAdded = onAdded
            }
        }
    }
}
