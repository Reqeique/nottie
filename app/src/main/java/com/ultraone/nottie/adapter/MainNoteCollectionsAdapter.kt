package com.ultraone.nottie.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ultraone.nottie.R
import com.ultraone.nottie.databinding.RmMainCollectionSmallBinding
import com.ultraone.nottie.databinding.RmMainNoteSmallBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.util.*
import com.ultraone.nottie.viewmodel.DataProviderViewModel

class NoteCollectionsAdapter: RecyclerView.Adapter< NoteCollectionsAdapter.ViewHolder>()  {
    var onItemClick: ((noteCollection: NoteCollections,pos: Int,v: View) -> Unit)? = null

    private var datas = listOf<NoteCollections>()

    lateinit var parent: RecyclerView

    private var listNotes = listOf<Note>()

    @SuppressLint("NotifyDataSetChanged")
    fun addList(datas: List<NoteCollections>, note: List<Note>) {
        this.datas = datas
        this.listNotes = note
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
        val root: MaterialCardView
        val pb: UAProgressBar
        val collectionName : TextView
        init {
            pb = binding.rMCSPB
            root = binding.rmMainCollectionSmallRoot
            collectionName = binding.rmMainCollectionSmallName
            binding.root.setOnClickListener {
                onItemClick?.invoke(datas[adapterPosition], adapterPosition, it)
            }
        }
    }
    override fun onBindViewHolder(holder: NoteCollectionsAdapter.ViewHolder, position: Int) {
        holder {
            root.transitionName = "createNewCollection-${datas[position].id}"
            pb.thumb.mutate().alpha = 0
            val kv = listNotes.filter { it.attachmentAndOthers?.collectionId == datas[position].id && it.deleted == false && it.attachmentAndOthers.archived == false }.map {
                (itemView.context.resolver(it.attachmentAndOthers?.color?.toIntOrNull()
                    ?: R.attr.colorPrimary)) to listNotes.filter {  it1 -> it1.attachmentAndOthers?.collectionId == datas[position].id}.filter { it1 -> it1.attachmentAndOthers?.color == it.attachmentAndOthers?.color} .size
            }
            val allItemSize = kv.map { it.second }.sum()
            initData(kv, allItemSize, pb)
            collectionName.text = datas[position].collectionName
        }
    }
    private fun initData(colorsAndItemSize: List<Pair<Int,Int>>, allItemSize: Int, seekBar: UAProgressBar ){
        val pIs = mutableListOf<ProgressItem>()
        //vaval pI = ProgressItem()
        colorsAndItemSize.forEach {

            val colors = it.first
            val itemSize = it.second
            Log.d("MNCA@75", "${itemSize.toFloat()/allItemSize.toFloat()} $itemSize, $allItemSize")
            val pI = ProgressItem(colors ,((itemSize.toFloat()/allItemSize.toFloat())*100).toFloat())
            pIs.add(pI)
        }
        Log.d("MNCA@78", "${arrayListOf(* (pIs.toTypedArray())).toList()}")
        seekBar.initData(mutableListOf(*(pIs.toTypedArray())))
       seekBar.invalidate()
    }
    override fun getItemCount(): Int = datas.size
}