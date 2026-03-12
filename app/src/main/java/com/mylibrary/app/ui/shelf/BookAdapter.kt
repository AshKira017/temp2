package com.mylibrary.app.ui.shelf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mylibrary.app.R
import com.mylibrary.app.data.Book
import com.mylibrary.app.databinding.ItemBookBinding

class BookAdapter(
    private val onClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.tvTitle.text = book.title
            binding.tvAuthor.text = book.author

            val noteCount = book.chapters.size
            if (noteCount > 0) {
                binding.tvBadge.visibility = android.view.View.VISIBLE
                binding.tvBadge.text = noteCount.toString()
            } else {
                binding.tvBadge.visibility = android.view.View.GONE
            }

            if (book.coverUrl.isNotBlank()) {
                Glide.with(binding.root)
                    .load(book.coverUrl)
                    .placeholder(R.drawable.bg_cover_placeholder)
                    .error(R.drawable.bg_cover_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.imgCover)
            } else {
                binding.imgCover.setImageResource(R.drawable.bg_cover_placeholder)
            }

            binding.root.setOnClickListener { onClick(book) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(a: Book, b: Book) = a.id == b.id
            override fun areContentsTheSame(a: Book, b: Book) = a == b
        }
    }
}
