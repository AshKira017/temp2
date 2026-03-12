package com.mylibrary.app.ui.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mylibrary.app.MainActivity
import com.mylibrary.app.data.LibraryManager
import com.mylibrary.app.databinding.FragmentNoteViewBinding

class NoteViewFragment : Fragment() {

    private var _binding: FragmentNoteViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookId: String
    private lateinit var chapterId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNoteViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookId = requireArguments().getString(ARG_BOOK_ID)!!
        chapterId = requireArguments().getString(ARG_CHAPTER_ID)!!
        refreshData()
    }

    private fun refreshData() {
        val book = LibraryManager.getBook(bookId) ?: return
        val chapter = book.chapters.find { it.id == chapterId } ?: return
        val idx = book.chapters.indexOfFirst { it.id == chapterId }

        binding.tvBookTitle.text = book.title
        binding.tvChapterTitle.text = chapter.title
        binding.tvDate.text = chapter.date
        binding.tvBody.text = chapter.note

        // Prev / Next
        val prev = book.chapters.getOrNull(idx - 1)
        val next = book.chapters.getOrNull(idx + 1)

        binding.btnPrev.visibility = if (prev != null) View.VISIBLE else View.INVISIBLE
        binding.btnNext.visibility = if (next != null) View.VISIBLE else View.INVISIBLE
        binding.btnPrev.text = "← ${prev?.title ?: ""}"
        binding.btnNext.text = "${next?.title ?: ""} →"

        binding.btnPrev.setOnClickListener {
            prev?.let { p ->
                (activity as MainActivity).navigate(newInstance(bookId, p.id))
            }
        }
        binding.btnNext.setOnClickListener {
            next?.let { n ->
                (activity as MainActivity).navigate(newInstance(bookId, n.id))
            }
        }

        binding.btnBack.setOnClickListener { (activity as MainActivity).goBack() }

        binding.btnEdit.setOnClickListener {
            (activity as MainActivity).navigate(NoteEditFragment.newInstance(bookId, chapterId))
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete summary?")
                .setMessage("\"${chapter.title}\" will be permanently deleted.")
                .setPositiveButton("Delete") { _, _ ->
                    LibraryManager.deleteChapter(bookId, chapterId)
                    (activity as MainActivity).goBack()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BOOK_ID = "book_id"
        private const val ARG_CHAPTER_ID = "chapter_id"

        fun newInstance(bookId: String, chapterId: String) = NoteViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_BOOK_ID, bookId)
                putString(ARG_CHAPTER_ID, chapterId)
            }
        }
    }
}
