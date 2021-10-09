package com.ultraone.nottie.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ultraone.nottie.databinding.RmMainCollectionSmallBinding
import com.ultraone.nottie.databinding.RmMainNoteSmallBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.util.invoke

class NoteCollectionsAdapter: RecyclerView.Adapter< NoteCollectionsAdapter.ViewHolder>()  {
    var onItemClick: ((noteCollection: NoteCollections,pos: Int,v: View) -> Unit)? = null
    private var datas = listOf<NoteCollections>()

    lateinit var parent: RecyclerView

    @SuppressLint("NotifyDataSetChanged")
    fun addList(datas: List<NoteCollections>) {
        this.datas = datas
        notifyDataSetChanged()
        if (datas.isNotEmpty()) {
            //   this.parent.smoothScrollToPosition(datas.size)
        }
    }
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        parent = recyclerView
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteCollectionsAdapter.ViewHolder {
        val binding = RmMainCollectionSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    inner class ViewHolder(binding: RmMainCollectionSmallBinding) : RecyclerView.ViewHolder(binding.root) {
        val collectionName : TextView
        init {
            val x = 0
            collectionName = binding.rmMainCollectionSmallName
            binding.root.setOnClickListener {
                onItemClick?.invoke(datas[adapterPosition], adapterPosition, parent)
            }
        }
    }
    override fun onBindViewHolder(holder: NoteCollectionsAdapter.ViewHolder, position: Int) {
        holder {
            collectionName.text = datas[position].collectionName
        }
    }

    override fun getItemCount(): Int = datas.size
}