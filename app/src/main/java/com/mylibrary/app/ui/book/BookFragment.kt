package com.mylibrary.app.ui.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mylibrary.app.MainActivity
import com.mylibrary.app.data.LibraryManager
import com.mylibrary.app.databinding.FragmentBookBinding
import com.mylibrary.app.ui.note.NoteEditFragment
import com.mylibrary.app.ui.note.NoteViewFragment

class BookFragment : Fragment() {

    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var bookId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookId = requireArguments().getString(ARG_BOOK_ID)!!

        chapterAdapter = ChapterAdapter(
            onView = { chapter ->
                (activity as MainActivity).navigate(NoteViewFragment.newInstance(bookId, chapter.id))
            },
            onEdit = { chapter ->
                (activity as MainActivity).navigate(NoteEditFragment.newInstance(bookId, chapter.id))
            },
            onDelete = { chapter ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete summary?")
                    .setMessage("\"${chapter.title}\" will be permanently deleted.")
                    .setPositiveButton("Delete") { _, _ ->
                        LibraryManager.deleteChapter(bookId, chapter.id)
                        refreshData()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChapters.adapter = chapterAdapter

        binding.btnBack.setOnClickListener { (activity as MainActivity).goBack() }

        binding.btnNewSummary.setOnClickListener {
            (activity as MainActivity).navigate(NoteEditFragment.newInstance(bookId, null))
        }

        binding.btnRemove.setOnClickListener {
            val book = LibraryManager.getBook(bookId) ?: return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("Remove book?")
                .setMessage("\"${book.title}\" and all its summaries will be removed.")
                .setPositiveButton("Remove") { _, _ ->
                    LibraryManager.removeBook(bookId)
                    (activity as MainActivity).goBack()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val book = LibraryManager.getBook(bookId) ?: return
        binding.tvTitle.text = book.title
        binding.tvAuthor.text = book.author
        binding.tvNoteCount.text = "${book.chapters.size} note${if (book.chapters.size != 1) "s" else ""}"

        if (book.coverUrl.isNotBlank()) {
            Glide.with(this).load(book.coverUrl)
                .error(com.mylibrary.app.R.drawable.bg_cover_placeholder)
                .into(binding.imgCover)
        }

        chapterAdapter.submitList(book.chapters.toList())
        binding.emptyChapters.visibility = if (book.chapters.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BOOK_ID = "book_id"
        fun newInstance(bookId: String) = BookFragment().apply {
            arguments = Bundle().apply { putString(ARG_BOOK_ID, bookId) }
        }
    }
}
