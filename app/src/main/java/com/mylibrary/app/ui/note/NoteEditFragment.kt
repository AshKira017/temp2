package com.mylibrary.app.ui.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mylibrary.app.MainActivity
import com.mylibrary.app.data.Chapter
import com.mylibrary.app.data.LibraryManager
import com.mylibrary.app.databinding.FragmentNoteEditBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteEditFragment : Fragment() {

    private var _binding: FragmentNoteEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookId: String
    private var chapterId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookId = requireArguments().getString(ARG_BOOK_ID)!!
        chapterId = requireArguments().getString(ARG_CHAPTER_ID)

        val book = LibraryManager.getBook(bookId)
        val chapter = chapterId?.let { cid -> book?.chapters?.find { it.id == cid } }

        binding.tvBookTitle.text = book?.title ?: ""
        binding.tvMode.text = if (chapter != null) "Editing summary" else "New summary"

        chapter?.let {
            binding.etChapterTitle.setText(it.title)
            binding.etNote.setText(it.note)
        }

        updateWordCount()
        binding.etNote.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { updateWordCount() }
        })

        binding.btnBack.setOnClickListener { (activity as MainActivity).goBack() }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun updateWordCount() {
        val words = binding.etNote.text.toString().trim()
            .split(Regex("\\s+")).filter { it.isNotBlank() }.size
        binding.tvWordCount.text = "$words word${if (words != 1) "s" else ""}"
    }

    private fun save() {
        val title = binding.etChapterTitle.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        if (title.isBlank()) {
            binding.etChapterTitle.error = "Chapter name required"
            return
        }
        if (note.isBlank()) {
            Toast.makeText(requireContext(), "Summary cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        val chapter = Chapter(
            id = chapterId ?: System.currentTimeMillis().toString(),
            title = title,
            note = note,
            date = date
        )
        LibraryManager.saveChapter(bookId, chapter)
        (activity as MainActivity).goBack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BOOK_ID = "book_id"
        private const val ARG_CHAPTER_ID = "chapter_id"

        fun newInstance(bookId: String, chapterId: String?) = NoteEditFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_BOOK_ID, bookId)
                putString(ARG_CHAPTER_ID, chapterId)
            }
        }
    }
}
