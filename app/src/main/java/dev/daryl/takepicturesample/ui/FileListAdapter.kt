package dev.daryl.takepicturesample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import dev.daryl.takepicturesample.data.FileListModel
import dev.daryl.takepicturesample.databinding.ItemFileListBinding

class FileListAdapter(
    private val onItemClicked: (FileListModel) -> Unit,
    private val onDeletePressed: (FileListModel) -> Unit
) :
    ListAdapter<FileListModel, FileListAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<FileListModel?>() {
        override fun areItemsTheSame(oldItem: FileListModel, newItem: FileListModel): Boolean {
            return oldItem.fileName == newItem.fileName
        }

        override fun areContentsTheSame(oldItem: FileListModel, newItem: FileListModel): Boolean {
            return oldItem == newItem
        }
    }) {

    inner class ViewHolder(val binding: ItemFileListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.imageDelete.setOnClickListener {
                onDeletePressed(binding.item!!)
            }
            binding.cslItem.setOnClickListener {
                onItemClicked(binding.item!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFileListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            this.item = item
            imagePreview.load(item.file)
            textFileName.text = item.fileName
        }
    }

}