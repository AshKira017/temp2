package com.mylibrary.app.ui.search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mylibrary.app.R
import com.mylibrary.app.data.BookSearchResult
import com.mylibrary.app.databinding.ItemSearchResultBinding

class SearchAdapter(
    private val isAdded: (BookSearchResult) -> Boolean,
    private val onAdd: (BookSearchResult) -> Unit
) : ListAdapter<BookSearchResult, SearchAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: BookSearchResult) {
            binding.tvTitle.text = result.title
            binding.tvAuthor.text = result.author

            Glide.with(binding.root)
                .load(result.coverUrl)
                .placeholder(R.drawable.bg_cover_placeholder)
                .error(R.drawable.bg_cover_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgCover)

            val added = isAdded(result)
            binding.btnAdd.text = if (added) "Added" else "+ Add"
            binding.btnAdd.isEnabled = !added
            binding.btnAdd.alpha = if (added) 0.5f else 1f

            binding.btnAdd.setOnClickListener {
                if (!isAdded(result)) onAdd(result)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<BookSearchResult>() {
            override fun areItemsTheSame(a: BookSearchResult, b: BookSearchResult) = a.id == b.id
            override fun areContentsTheSame(a: BookSearchResult, b: BookSearchResult) = a == b
        }
    }
}
