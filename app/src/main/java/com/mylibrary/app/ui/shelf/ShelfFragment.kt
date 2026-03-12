package com.mylibrary.app.ui.shelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mylibrary.app.MainActivity
import com.mylibrary.app.data.LibraryManager
import com.mylibrary.app.databinding.FragmentShelfBinding
import com.mylibrary.app.ui.book.BookFragment
import com.mylibrary.app.ui.search.SearchFragment
import com.mylibrary.app.ui.settings.SettingsFragment

class ShelfFragment : Fragment() {

    private var _binding: FragmentShelfBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BookAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BookAdapter { book ->
            (activity as MainActivity).navigate(BookFragment.newInstance(book.id))
        }

        binding.recyclerBooks.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerBooks.adapter = adapter

        binding.btnAdd.setOnClickListener {
            (activity as MainActivity).navigate(SearchFragment())
        }
        binding.btnSettings.setOnClickListener {
            (activity as MainActivity).navigate(SettingsFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val books = LibraryManager.getBooks()
        adapter.submitList(books)
        binding.emptyState.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerBooks.visibility = if (books.isEmpty()) View.GONE else View.VISIBLE
        binding.tvBookCount.text = "${books.size} book${if (books.size != 1) "s" else ""}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
