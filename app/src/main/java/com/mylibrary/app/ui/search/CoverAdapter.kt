package com.mylibrary.app.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mylibrary.app.R
import com.mylibrary.app.data.CoverResult
import com.mylibrary.app.databinding.ItemCoverBinding

class CoverAdapter(
    private val onSelect: (String) -> Unit
) : ListAdapter<CoverResult, CoverAdapter.ViewHolder>(DIFF) {

    private var selectedUrl: String? = null

    fun setSelected(url: String) {
        selectedUrl = url
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemCoverBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CoverResult) {
            Glide.with(binding.root)
                .load(item.url)
                .placeholder(R.drawable.bg_cover_placeholder)
                .error(R.drawable.bg_cover_placeholder)
                .into(binding.imgCover)

            binding.tvSource.text = item.source
            val isSelected = item.url == selectedUrl
            binding.root.strokeWidth = if (isSelected) 3 else 0
            binding.root.strokeColor = binding.root.context.getColor(R.color.accent)

            binding.root.setOnClickListener {
                selectedUrl = item.url
                onSelect(item.url)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCoverBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CoverResult>() {
            override fun areItemsTheSame(a: CoverResult, b: CoverResult) = a.url == b.url
            override fun areContentsTheSame(a: CoverResult, b: CoverResult) = a == b
        }
    }
}
