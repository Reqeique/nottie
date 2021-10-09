package com.ultraone.nottie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.fetch.VideoFrameUriFetcher
import coil.request.ImageRequest
import com.ultraone.nottie.databinding.RmNoteTakingAttachementsBinding
import com.ultraone.nottie.util.*
import com.ultraone.nottie.util.NormalFrameUriFetcher

class NoteTakingAttachmentAdapter(): RecyclerView.Adapter<NoteTakingAttachmentAdapter.ViewHolder>() {
    var onItemClick: ((uri: String ,pos: Int,v: View) -> Unit)? = null
    private var datas = listOf<String>()
    inner class ViewHolder(val binding: RmNoteTakingAttachementsBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(datas[adapterPosition], adapterPosition, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RmNoteTakingAttachementsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      //  when()
        val data = (datas[position])
        val builder = ImageRequest.Builder(holder.itemView.context)

        when(data.fileType(holder.itemView.context)) {
            is AUDIO -> {
                holder.binding.imageView.loadPreview(data) {
                    fetcher(NormalFrameUriFetcher(holder.itemView.context))
                }


            }
            is DOCUMENT -> {

            }
            is IMAGE -> {
                holder.binding.imageView.loadPreview(data)
            }
            is Other -> TODO()
            is VIDEO -> {
                holder.binding.imageView.apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    loadPreview(data) {
                        fetcher(VideoFrameUriFetcher(context))
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int = datas.size
    fun addList(datas: List<String>){
        this.datas = datas
        notifyDataSetChanged()

    }
}
