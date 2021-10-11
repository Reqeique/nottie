package com.ultraone.nottie.fragment.notetaking

import android.content.Context
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.discord.simpleast.core.simple.SimpleRenderer
import com.google.android.material.transition.MaterialContainerTransform
import com.ultraone.nottie.R
import com.ultraone.nottie.adapter.NoteTakingAttachmentAdapter

import com.ultraone.nottie.databinding.FragmentNoteTakingNewBinding
import com.ultraone.nottie.databinding.FragmentNoteTakingNewColorChooserDialogBinding
import com.ultraone.nottie.fragment.notetaking.sheet.NoteTakingFragmentSheet
import com.ultraone.nottie.fragment.notetaking.sheet.NoteTakingFragmentSheetNewCollection
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteAttachmentAndOther
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.*
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import com.ultraone.nottie.viewmodel.NoteTakingFragmentViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.concurrent.Executors
import kotlin.contracts.ExperimentalContracts

@ExperimentalStdlibApi
class NoteTakingFragment : Fragment() {
    companion object {

        const val TAG = "::NoteTakingF"
    }

    private lateinit var notesClient: List<Note>
    private lateinit var attachmentAdapter: NoteTakingAttachmentAdapter
    private lateinit var attachmentAndOther: NoteAttachmentAndOther
    private lateinit var binding: FragmentNoteTakingNewBinding
    private val noteTakingFragmentViewModel: NoteTakingFragmentViewModel by activityViewModels()
    private val dataProvider: DataProviderViewModel by activityViewModels()
    private val args: NoteTakingFragmentArgs by navArgs()
    private fun setupMarkdown() {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    //    private lateinit var changingTitle: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragmentContainerViewerMain
            duration = 300L

            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().resolver(R.attr.colorSurface))
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragmentContainerViewerMain
            duration = 300L

            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().resolver(R.attr.colorSurface))
        }
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            Log.d("$TAG@110", "URI = $it")
        }


    }

//    lateinit var note: Note

    @ExperimentalContracts
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteTakingNewBinding.inflate(inflater, container, false)

//            requireActivity().window.navigationBarColor = ContextCompat.getColor(requireActivity(), R.color.gray_00)

        lifecycleScope.launch(Main) {
            attachmentAdapter = NoteTakingAttachmentAdapter()
            binding.fNTNAttachmentRecycler.adapter = attachmentAdapter


            setUpNoteId()
            setUpNoteClient()


            binding.configureAddButton()
            binding.fNTNBackButton.setOnClickListener {

                launch {

//                    findNavController().let {
//                        it.navigate(NoteTakingFragmentDirections.actionNoteTakingFragmentToMainFragment())
//                        it.popBackStack()
//
//                    }
                }
            }
            binding.fNTNCollection.setOnClickListener {
                NoteTakingFragmentSheetNewCollection().show(
                    childFragmentManager,
                    NoteTakingFragmentSheetNewCollection().tag
                )
            }


            val _note = args.note
            binding.fNTNNotes.text = _note?.mainNote?.toEditable()
            binding.fNTNTitle.text = _note?.title?.toEditable()


            observeForChanges(inflater = inflater)

        }

        return binding.root
    }

    /**
     * function [observeForChanges] used to listen for different form of changes i.e text , state and apply [updateOrCreateNew] note
     * */
    @ExperimentalStdlibApi
    private fun observeForChanges(
        note: Note = Note(
            0, null, null, null, NoteAttachmentAndOther(
                null,
                1,
                null,


                mutableListOf(),
                null
            ), false
        ), inflater: LayoutInflater
    ) = with(binding) {
        var copyable = note


        fNTNTitle.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) return@doOnTextChanged
            Log.d(
                "$TAG@134",
                noteTakingFragmentViewModel.noteId.value.toString() + text.toString()
            )
            copyable = copyable.copy(title = text.toString())
            updateOrCreateNew(copyable)


        }
        fNTNNotes.doOnTextChanged { text, _, _, _ ->
            if (text == null) return@doOnTextChanged
            copyable = copyable.copy(mainNote = text.toString())
            updateOrCreateNew(copyable)
        }

        fNTNPinButton.invokeSelectableState<ImageButton> {
            copyable =
                copyable.copy(attachmentAndOthers = copyable.attachmentAndOthers?.copy(pinned = it))
            Log.d("$TAG@154", "pinned = $it, copiable = $copyable")
            updateOrCreateNew(copyable)
        }
        fragmentNoteTakingNewChooseColor.setOnClickListener {
            Log.d(
                "$TAG@110", "clicked" +
                        ""
            )
            val binding = FragmentNoteTakingNewColorChooserDialogBinding.inflate(inflater)

            requireContext().dialog(
                { requestWindowFeature(Window.FEATURE_NO_TITLE) },
                binding.root,
                {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val views = arrayOf(binding.fNTNCCDBlue, binding.fNTNCCDRed, binding.fNTNCCDPink)
                    val clr = arrayOf(resources.getColor(R.color.blue_500), resources.getColor(R.color.red_500), resources.getColor(R.color.pink_500))
                    views.forEachIndexed { index, cardView ->
                        val color = clr[index]
                        cardView.setOnClickListener {

                                updateOrCreateNew(
                                    copyable.copy(
                                        attachmentAndOthers = copyable.attachmentAndOthers?.copy(
                                            color = color.toString()
                                        )
                                    )
                                )

                        }
                    }
                    show()
                })
        }
        lifecycleScope.launch {
            launch {

                noteTakingFragmentViewModel.uriListener.collect { result ->
                    if (result == null) return@collect

                    copyable = copyable.copy(
                        attachmentAndOthers = copyable.attachmentAndOthers!!.copy(
                            fileUri = notesClient.filterById(noteTakingFragmentViewModel.noteId.value!!).attachmentAndOthers!!.fileUri.update(
                                result.data.toString()
                            )
                        )
                    )
                    Log.d("$TAG@200", "copyable = $copyable.toS")
                    updateOrCreateNew(copyable)


                }
//           \
            }
            launch {
                dataProvider.getAllCollections()
                noteTakingFragmentViewModel.collectionId.collect { id ->
                    if (id == null) return@collect
                    copyable = copyable.copy(
                        attachmentAndOthers = copyable.attachmentAndOthers?.copy(collectionId = id)
                    )


                    updateOrCreateNew(copyable)
                }
            }

        }


    }

    /**
     * function [updateOrCreateNew] used to weather create new or update existing note based on [NoteTakingFragmentViewModel.noteId]
     * */
    private fun updateOrCreateNew(note: Note) {

        when {
            noteTakingFragmentViewModel.noteId.value == NULL_VALUE_INT -> {
                //ADD

                binding.new(
                    note.copy(
                        dateTime = currentTime,
                        attachmentAndOthers = note.attachmentAndOthers?.copy(
                            collectionId = 1,
                            fileUri = mutableListOf()
                        )
                    )
                )

            }
            noteTakingFragmentViewModel.noteId.value != NULL_VALUE_INT -> {
                //UPDATE
                lifecycleScope.launch {
                    Log.d(
                        "$TAG@145",
                        "D = ${noteTakingFragmentViewModel.noteId.value}"
                    )
                    if (!::notesClient.isInitialized) return@launch
                    notesClient.let { itList ->
                        val itNote = itList.first { itNote ->
                            itNote.id == noteTakingFragmentViewModel.noteId.value
                        }
                        Log.d("$TAG@193", "itNote = $itNote , $note")

                        binding.update(
                            itNote.copy(
                                title = note.title ?: itNote.title,
                                mainNote = note.mainNote ?: itNote.mainNote,
                                dateTime = currentTime,
                                attachmentAndOthers = NoteAttachmentAndOther(
                                    note.attachmentAndOthers?.archived
                                        ?: itNote.attachmentAndOthers?.archived,
                                    note.attachmentAndOthers?.collectionId
                                        ?: itNote.attachmentAndOthers?.collectionId ?: 1,
                                    note.attachmentAndOthers?.pinned
                                        ?: itNote.attachmentAndOthers?.pinned,
                                    note.attachmentAndOthers?.fileUri.takeIf { it!!.isNotEmpty() }
                                        ?: itNote.attachmentAndOthers!!.fileUri,

                                    note.attachmentAndOthers?.color
                                        ?: itNote.attachmentAndOthers?.color
                                ),
                                deleted = note.deleted ?: itNote.deleted
                            )
                        )
                        Log.d("$TAG@153", itList.toString())


                    }
                }


            }
        }

    }

    private suspend fun setUpNoteClient() = with(binding) {


        dataProvider.getAllNote.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                it.collect { list ->

                    notesClient = list


                    launch {

                        val data = list.filterByIdOrNull(noteTakingFragmentViewModel.noteId.value)
                        setUpCollection(data)
                        setUpRecyclers(data)
                        setUpPinButton(data)


                    }

                }
            }
        }
    }

    private suspend fun FragmentNoteTakingNewBinding.setUpCollection(data: Note?) {
        noteTakingFragmentViewModel.collectionId.emit(
            data?.attachmentAndOthers?.collectionId ?: 1
        )
        dataProvider.getAllCollections().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.FAILED -> {

                }
                is Result.LOADING -> {


                }
                is Result.NULL_VALUE -> {

                }
                is Result.SUCCESS<*> -> {
                    val collectionsFlow = result.data as Flow<List<NoteCollections>>
                    lifecycleScope.launch {
                        collectionsFlow.collect { collections ->
                            fNTNCollectionText.text =
                                collections.filterByIdOrNull(data?.attachmentAndOthers?.collectionId)?.collectionName
                        }
                    }
                }
            }

        }
    }

    private suspend fun setUpNoteId() {
        noteTakingFragmentViewModel.noteId.emit(args.id)
    }

    /**
     * function [new] is used to insert new note
     * */
    private fun FragmentNoteTakingNewBinding.new(note: Note) = with(lifecycleScope) {
        launch {
            //if(args.id == NULL_VALUE_INT){
            dataProvider.addNote(note).observe(viewLifecycleOwner) {
                lifecycleScope.launch(Main) {
                    when (it) {
                        is Result.FAILED -> root.shortSnackBar("Failed")
                        is Result.LOADING -> root.shortSnackBar("Updating")
                        is Result.NULL_VALUE -> root.shortSnackBar("Null Value")
                        is Result.SUCCESS<*> -> {
                            val data = it.data as Long
                            Log.d("$TAG@129", "$data")


                            noteTakingFragmentViewModel.noteId.value = data.toInt()
                        }

                    }
                }
            }
            // }
        }
    }

    /**
     * function [update] is used to update existing note with declared id
     * */
    private fun FragmentNoteTakingNewBinding.update(note: Note) = with(lifecycleScope) {
        launch {

            lifecycleScope.launch {

                dataProvider.updateNote(note).observe(viewLifecycleOwner) {
                    lifecycleScope.launch(Main) {
                        when (it) {
                            is Result.FAILED -> root.shortSnackBar("Failed")
                            is Result.LOADING -> root.shortSnackBar("Updating")
                            is Result.NULL_VALUE -> root.shortSnackBar("Null Value")
                            is Result.SUCCESS<*> -> {
                                root.shortSnackBar("Updated")
                            }

                        }
                    }
                }
            }


        }


    }


    /**[configureAddButton] is used to invoke click  listener to [FragmentNoteTakingNewBinding.fNTNAddButton]*/
    private fun FragmentNoteTakingNewBinding.configureAddButton() {
        fNTNAddButton.setOnClickListener {
            NoteTakingFragmentSheet().let {
                it.show(superFragmentManager, it.tag)
            }

        }
    }

    private fun FragmentNoteTakingNewBinding.setUpRecyclers(data: Note?) {

        Log.d("$TAG@418", "${data?.attachmentAndOthers?.fileUri}")
        if (data?.attachmentAndOthers?.fileUri != emptyList<String>() && data?.attachmentAndOthers?.fileUri?.isNotEmpty() == true) {
            fNTNAttachmentRecycler.visibility = View.VISIBLE
            attachmentAdapter.addList(data.attachmentAndOthers.fileUri.toList().map { it!! })
        }
    }

    private fun FragmentNoteTakingNewBinding.setUpPinButton(data: Note?) {
        if (data != null && data.attachmentAndOthers?.pinned == true) {
            fNTNPinButton.isSelected = true
        } else if (data == null || data.attachmentAndOthers?.pinned == false) {
            fNTNPinButton.isSelected = false
        }
    }

    override fun onDetach() {
        super.onDetach()

        Log.d("$TAG@353", "ON DETACH calling null")
        noteTakingFragmentViewModel.uriListener.value = null

    }

    private fun FragmentNoteTakingNewBinding.setUpColorChooser(
        inflater: LayoutInflater,
        note: Note
    ) {

    }

    private fun reFormatter(text: CharSequence): String? {
        var formattedText: String? = text.toString()

        SpannableString(SimpleRenderer.renderBasicMarkdown(text)).let { spannedString ->
            spannedString.getSpans(0, text.length, StyleSpan::class.java).asList()
                .let { itList ->
                    val builder = StringBuilder(formattedText)
                    itList.map { t ->
                        (spannedString.getSpanStart(t) to spannedString.getSpanEnd(
                            t
                        )) to t.style
                    }
                        .forEach {
                            Log.d("LOG-G", "$it")
                            when (it.second) {
                                1 -> {
                                    formattedText = builder.insert(it.first.first, "**")
                                        .insert(it.first.second + 2, "**").toString()
                                }

                            }
                        }
                }
        }

        Log.d("LOG-", "bruh $formattedText")
        return formattedText

    }

    private fun useLessCode() {
        //   fNTNNotes2.visibility = View.GONE
//            var shouldIgnoreChanges = false
//
//            fNTNNotes.doOnTextChanged { changingText, start, count, after ->
//                if (changingText != null) {
//                    if (changingText.isNotEmpty()) {
//                        fNTNNotes.setSelection(after)
//                        //  fNTNNotes.setSelection(changingText.length)
//                        if (!shouldIgnoreChanges) {
//
//                            shouldIgnoreChanges = true
//                            fNTNNotes.text =
//                                SimpleRenderer.renderBasicMarkdown(changingText)
//                            shouldIgnoreChanges = false
//                        }
//
//
//
//                        Log.d(
//                            "LOG-G",
//                            "h ${reFormatter(changingText)}"
//                        )
//
//                        fNTNBackButton
//                        fNTNBackButton.visibility = View.GONE
//                        fNTNSaveButton.visibility = View.VISIBLE
//                    } else if (changingText.isNullOrEmpty()) {
//                        fNTNBackButton.visibility = View.VISIBLE
//                        fNTNSaveButton.visibility = View.GONE
//                    }
//
//                }
//            }
        //        val markwon =  Markwon.create(requireContext())
//        binding.fNTNNotes.doOnTextChanged { text, start, before, count ->
////            if (text != null) {
////                SimpleRenderer.renderBasicMarkdown(text).let {
////                    Toast.makeText(requireContext(),it, Toast.LENGTH_SHORT).show()
////                }
////            }
//
//            markwon.toMarkdown(text.toString()).let {
//                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
//                binding.fNTNE.text = it.toEditable()
//            }
//        }
        val markwon = Markwon.create(requireContext());

        // create editor
        val markwonEditor = MarkwonEditor.create(markwon);

        val markwonTextWatcher = MarkwonEditorTextWatcher.withPreRender(
            markwonEditor, Executors.newCachedThreadPool(),
            binding.fNTNE
        )
        //  binding.fNTNE.addTextChangedListener(markwonTextWatcher)

    }


}
