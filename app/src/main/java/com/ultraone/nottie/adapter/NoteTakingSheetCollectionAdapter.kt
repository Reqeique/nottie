package com.ultraone.nottie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ultraone.nottie.databinding.RmNoteTakingSheetNewCollectionBinding
import com.ultraone.nottie.model.NoteCollections

class NoteTakingSheetCollectionAdapter: RecyclerView.Adapter<NoteTakingSheetCollectionAdapter.ViewHolder>() {
    var onItemClick: ((collection: NoteCollections,pos: Int,v: View) -> Unit)? = null
    private var datas = listOf<NoteCollections>()
    inner class ViewHolder(val binding: RmNoteTakingSheetNewCollectionBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(datas[adapterPosition], adapterPosition, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RmNoteTakingSheetNewCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         holder.binding.rNTSNC.text = datas[position].collectionName
    }

    override fun getItemCount(): Int = datas.size
    fun addList(datas: List<NoteCollections>){
        this.datas = datas
        notifyDataSetChanged()

    }
}
