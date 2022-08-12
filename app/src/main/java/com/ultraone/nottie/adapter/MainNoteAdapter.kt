package com.ultraone.nottie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ultraone.nottie.R
import com.ultraone.nottie.databinding.RmDateTimeBinding
import com.ultraone.nottie.databinding.RmMainNoteBinding
import com.ultraone.nottie.databinding.RmMainNoteSmallBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.util.*

//class SwipeToDelete : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper) {
//    override fun onMove(
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        target: RecyclerView.ViewHolder
//    ): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        TODO("Not yet implemented")
//    }
//
//}
class MainNoteAdapter(
    /** val datas: List<Note>*/
) : RecyclerView.Adapter<MainNoteAdapter.ViewHolder>() {
    var onItemClick: ((Note, Int, View) -> Unit)? = null
    private var datas = listOf<Note>()

    lateinit var parent: RecyclerView

    @SuppressLint("NotifyDataSetChanged")
    fun addList(datas: List<Note>) {
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

    override fun getItemCount(): Int = datas.size
    inner class ViewHolder(binding: RmMainNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        val note: TextView
        val root: MaterialCardView
        val title: TextView
        val date: TextView
        val indicator: MaterialCardView

        init {
            val x = 0
            root = binding.rmMainRoot
            indicator = binding.rmMainIndicator
            date = binding.rmMainDate
            note = binding.rmMainNote
            title = binding.rmMainTitle
            root.setOnClickListener {
                onItemClick?.invoke(datas[adapterPosition], adapterPosition, it)
            }

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = position
        holder.apply {
            root.transitionName = "createNewNote${datas[p].id}"
            note.text = datas[p].mainNote
            title.text = datas[p].title
            date.text = datas[p].dateTime?.decodeToTimeAndDate()
            root.setOnLongClickListener {


                @ColorInt val color = holder.itemView.context.resolver(R.attr.colorPrimary)
                val r2 = root.resources.getColor(R.color.gray_00)
                root.setCardBackgroundColor(color)
                indicator.setCardBackgroundColor(r2)
                false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RmMainNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

}

class MyItemDetailLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as NoteAdapter.ViewHolder).getItemDetails()
        }
        return null
    }

}

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {
    var onItemClick: ((Note, Int, View) -> Unit)? = null
    private var datas = listOf<Note>()
    var tracker: SelectionTracker<Long>? = null

    lateinit var parent: RecyclerView

    @SuppressLint("NotifyDataSetChanged")
    fun addList(datas: List<Note>) {
        if (datas.size > 5) this.datas = datas.subList(0, 5) else this.datas = datas
        notifyDataSetChanged()
        if (datas.isNotEmpty()) {
            //   this.parent.smoothScrollToPosition(datas.size)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        parent = recyclerView
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = datas.size
    inner class ViewHolder(binding: RmMainNoteSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val card: CardView
        val note: TextView
        val root: MaterialCardView
        val title: TextView
        val date: TextView
        val cv: MaterialCardView
        val image: ImageView
        val document: ImageView
        val video: ImageView
        val audio: ImageView
        val indicator: MaterialCardView
        val imageView: ImageView
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long = datas[adapterPosition].id.toLong()

            }

        fun bind(v: Int, isActivated: Boolean?) {

        }

        init {
            val x = 0
            image = binding.rmMainNoteSmallImage
            document = binding.rmMainNoteSmallFile
            video = binding.rmMainNoteSmallVideo
            audio = binding.rmMainNoteSmallAudio
            cv = binding.rmMainNoteSmallCv
            card = binding.card
            imageView = binding.rMNSImageView
            root = binding.rmMainNoteSmallRoot
            indicator = binding.rmMainNoteSmallIndicator
            date = binding.rmMainNoteSmallDate
            note = binding.rmMainNoteSmallNote
            title = binding.rmMainNoteSmallTitle
            root.setOnClickListener {
                onItemClick?.invoke(datas[adapterPosition], adapterPosition, it)
            }
        }
    }
    private inline fun  <reified T: FileType> List<String>.containsFile(context: Context): Boolean{

        return any {

            it.fileType(context) is T
        }
    }

    private fun ViewHolder.setUpAttachments(p: Int){
        val attachmentAndOthers = (datas[p].attachmentAndOthers) ?: return
        //Log.d("")
        if (attachmentAndOthers.color != null) {

             cv.setCardBackgroundColor(
                itemView.context.resolver(attachmentAndOthers.color.toInt())
            )
        }
        image.visibility = View.GONE
        card.visibility = View.GONE
        document.visibility = View.GONE
        video.visibility = View.GONE
        audio.visibility = View.GONE

        if(attachmentAndOthers.fileUri.isNotEmpty()){
            val uri = attachmentAndOthers.fileUri.map {it!!}

                if(uri.containsFile<IMAGE>(itemView.context) ){
                    image.visibility = View.VISIBLE
                    card.visibility = View.VISIBLE
                    imageView.setImageURI(attachmentAndOthers.fileUri.first{
                        it!!.fileType(itemView.context) is IMAGE
                    }?.toUri())
                }
                if(uri.containsFile<AUDIO>(itemView.context)) {
                    audio.visibility = View.VISIBLE
                }

                if(uri.containsFile<VIDEO>(itemView.context)) {
                    video.visibility = View.VISIBLE
                }

                if(uri.containsFile<DOCUMENT>(itemView.context)) {

                    document.visibility = View.VISIBLE
                }



        }else if(attachmentAndOthers.fileUri.isEmpty()){
            image.visibility = View.GONE
            card.visibility = View.GONE
            document.visibility = View.GONE
            video.visibility = View.GONE
            audio.visibility = View.GONE
        }

//        if (attachmentAndOthers.fileUri.isEmpty() || !(attachmentAndOthers.fileUri.any {
//                it!!.fileType(itemView.context) is DOCUMENT
//            })) {
//                document.visibility = View.GONE
//
//        } else if (attachmentAndOthers.fileUri.isNotEmpty() && attachmentAndOthers.fileUri.any {
//                it!!.fileType(itemView.context) is DOCUMENT
//            }) {
//
//
//        }
//        if (attachmentAndOthers.fileUri.isEmpty() || !(attachmentAndOthers.fileUri.any {
//            it!!.fileType(itemView.context) is VIDEO
//        })) {
//
//
//        } else if (attachmentAndOthers.fileUri.isNotEmpty() && attachmentAndOthers.fileUri.any {
//            it!!.fileType(itemView.context) is VIDEO
//        }) {
//            video.visibility = View.VISIBLE
//
//        }
//        if (attachmentAndOthers.fileUri.isEmpty() || !(attachmentAndOthers.fileUri.any {
//            it!!.fileType(itemView.context) is AUDIO
//        })) {
//
//
//        } else if (attachmentAndOthers.fileUri.isNotEmpty() && attachmentAndOthers.fileUri.any {
//            it!!.fileType(itemView.context) is AUDIO
//        }) {
//            audio.visibility = View.VISIBLE
//
//        }
//        if (attachmentAndOthers.pinned == true) {
//            root.setCardBackgroundColor(root.context.resolver(R.attr.colorPrimary))
//            pin.visibility = View.VISIBLE
//        } else if (attachmentAndOthers.pinned == false) {
//            pin.visibility = View.GONE
//            if(attachmentAndOthers.color != null) return
//            root.setCardBackgroundColor(root.context.resolver(R.attr.colorSurface))
//        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        this.parent.smoothScrollToPosition(position + ((1..3).random()))
        val p = position
        holder.apply {
            note.text = datas[p].mainNote
            title.text = datas[p].title
            //todo uncomment the code below
            setUpAttachments(p)

//            date.text = datas[p].dateTime?.decodeToTimeAndDate()
            root.transitionName = "createNewNote${datas[p].dateTime}"
          //  pin.visibility = View.GONE

            when {
                tracker?.isSelected((datas[p].id).toLong()) == true -> {
                    @ColorInt val color = holder.itemView.context.resolver(R.attr.colorPrimary)
//                    val r2 = root.resources.getColor(R.color.gray_00)
                    root.setCardBackgroundColor(color)
                }
            }
//            root.setOnLongClickListener {
//
//
//
//                @ColorInt val color =   holder.itemView.context.resolver(R.attr.colorPrimary)
//                val r2 = root.resources.getColor(R.color.gray_00)
//                root.setCardBackgroundColor(color)
//                indicator.setCardBackgroundColor(r2)
//                false
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RmMainNoteSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

}

class DateTimeAdapter : RecyclerView.Adapter<DateTimeAdapter.ViewHolder>() {
    private var datas = (1..31).toList()

    lateinit var parent: RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        parent = recyclerView
    }

    @SuppressLint("NotifyDataSetChanged")


    override fun getItemCount(): Int = datas.size
    inner class ViewHolder(binding: RmDateTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val date: TextView
        val root2: MaterialCardView

        //        fun byPosition(pos: Int): View{
//
//        }
        init {
            val x = 0
            date = binding.rmDateTimeDate
            root2 = binding.root2
        }
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        this.parent.smoothScrollToPosition(currentTime.decodeDate() + ((1..3).random()))
        val v = parent.findViewHolderForAdapterPosition(currentTime.decodeDate() - 1)?.itemView
        val p = position
        v?.findViewById<MaterialCardView>(R.id.root2)
            ?.setCardBackgroundColor(ColorStateList.valueOf(holder.itemView.context.resolver(R.attr.colorTertiary)))

        holder {
            date.text = datas[p].toString().toEditable()
            root2.setOnClickListener {
                it.shortSnackBar(p.toString())
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RmDateTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
}
