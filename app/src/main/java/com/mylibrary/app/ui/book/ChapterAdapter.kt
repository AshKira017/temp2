package com.mylibrary.app.ui.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mylibrary.app.data.Chapter
import com.mylibrary.app.databinding.ItemChapterBinding

class ChapterAdapter(
    private val onView: (Chapter) -> Unit,
    private val onEdit: (Chapter) -> Unit,
    private val onDelete: (Chapter) -> Unit
) : ListAdapter<Chapter, ChapterAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chapter: Chapter) {
            binding.tvChapterTitle.text = chapter.title
            binding.tvDate.text = chapter.date
            binding.tvPreview.text = chapter.note

            binding.root.setOnClickListener { onView(chapter) }
            binding.btnEdit.setOnClickListener { onEdit(chapter) }
            binding.btnDelete.setOnClickListener { onDelete(chapter) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Chapter>() {
            override fun areItemsTheSame(a: Chapter, b: Chapter) = a.id == b.id
            override fun areContentsTheSame(a: Chapter, b: Chapter) = a == b
        }
    }
}
