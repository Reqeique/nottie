package com.ultraone.nottie.fragment

import android.content.Context
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

import com.ultraone.nottie.databinding.FragmentNoteTakingNewBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteAttachmentAndOther
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.*
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import com.ultraone.nottie.viewmodel.NoteTakingFragmentViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import java.util.concurrent.Executors
import kotlin.contracts.ExperimentalContracts

class NoteTakingFragment : Fragment() {
    companion object {

        const val TAG = "::NoteTakingF"
    }

    private lateinit var notesClient: List<Note>
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


            setUpNoteId()



            binding.configureAddButton()
            binding.fNTNBackButton.setOnClickListener {
                launch {
                    var hecked = (1..10).random()
                    noteTakingFragmentViewModel.noteId.emit(hecked)
//                    findNavController().let {
//                        it.navigate(NoteTakingFragmentDirections.actionNoteTakingFragmentToMainFragment())
//                        it.popBackStack()
//
//                    }
                }
            }


            val _note = args.note
            binding.fNTNNotes.text = _note?.mainNote?.toEditable()
            binding.fNTNTitle.text = _note?.title?.toEditable()
            var copyable = Note(
                0, null, null, null, NoteAttachmentAndOther(
                    null,
                    null,
                    null,
                    null,
                    null
                ), false
            )


            binding.fNTNTitle.doOnTextChanged { text, _, _, _ ->
                if (text.isNullOrBlank()) return@doOnTextChanged
                Log.d(
                    "$TAG@134",
                    noteTakingFragmentViewModel.noteId.value.toString() + text.toString()
                )
                copyable = copyable.copy(title = text.toString())
                updateOrCreateNew(copyable)


            }
            binding.fNTNNotes.doOnTextChanged { text, _, _, _ ->
                if (text.isNullOrBlank()) return@doOnTextChanged
                copyable = copyable.copy(mainNote = text.toString())
                updateOrCreateNew(copyable)
            }
            binding.fNTNPinButton.invokeSelectableState<ImageButton> {
                copyable =
                    copyable.copy(attachmentAndOthers = copyable.attachmentAndOthers?.copy(pinned = it))
                Log.d("$TAG@154", "pinned = $it, copiable = $copyable")
                updateOrCreateNew(copyable)
            }

            setUpNoteClient()
        }








        return binding.root
    }

    private fun updateOrCreateNew(note: Note) {

        when {
            noteTakingFragmentViewModel.noteId.value == NULL_VALUE_INT -> {
                //ADD

                binding.new(note.copy(dateTime = currentTime))

            }
            noteTakingFragmentViewModel.noteId.value != NULL_VALUE_INT -> {
                //UPDATE
                lifecycleScope.launch {
                    Log.d(
                        "$TAG@145",
                        "D = ${noteTakingFragmentViewModel.noteId.value}"
                    )
                    notesClient.let { itList ->
                        val itNote = itList.first { itNote ->
                            itNote.id == noteTakingFragmentViewModel.noteId.value
                        }

                        binding.update(
                            itNote.copy(
                                title = note.title ?: itNote.title,
                                mainNote = note.mainNote ?: itNote.mainNote,
                                dateTime = currentTime,
                                attachmentAndOthers = note.attachmentAndOthers ?: itNote.attachmentAndOthers,
                                deleted = note.deleted ?: itNote.deleted
                            )
                        )
                        Log.d("$TAG@153", itList.toString())


                    }
                }


            }
        }

    }

    private suspend fun lamdBda(block: (Note) -> Unit) = with(binding) {
        noteTakingFragmentViewModel.noteId.collect {
            if (it == null) return@collect
            Log.d("$TAG@202", "id = $it")
            val note = Note(0, null, null, null, null, false)
            //  block(note)
        }

    }

    private suspend fun setUpNoteClient() = with(binding) {
        dataProvider.getAllNote.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                it.collect { list ->
                    notesClient = list

                    val data = list.firstOrNull {
                        it.id == noteTakingFragmentViewModel.noteId.value
                    }

                    if (data !=null && data.attachmentAndOthers?.pinned == true) {
                        fNTNPinButton.isSelected = true
                    } else if (data == null || data.attachmentAndOthers?.pinned == false) {
                        fNTNPinButton.isSelected = false
                    }

                }
            }
        }
    }

    private suspend fun setUpNoteId() {
        //  noteTakingFragmentViewModel.noteId.postValue(args.id)
        noteTakingFragmentViewModel.noteId.emit(args.id)
    }

    /**function [observeNoteFirst] used to observe for the note that has been declared before or return new note*/
    private suspend fun observeNoteFirst(block: (Note) -> Unit) = with(binding) {
        // if (argsId == null) return@collect

        val note = Note(
            0,
            null,
            null,
            null,
            null,
            false
        )
        block(note)
//                    if (args.id == NULL_VALUE_INT) {
//
//                        block(note)
//
//
//                    } else if (args.id != NULL_VALUE_INT) {
//                        Log.d("$TAG@217", true.toString())
//                        lifecycleScope.launch {
//
//                            dataProvider.getAllNotes().observe(viewLifecycleOwner) {
//                                lifecycleScope.launch(Main) {
//                                    when (it) {
//
//                                        is Result.SUCCESS<*> -> {
//                                            val data = it.data as Flow<List<Note>>
//
//                                            data.collect {
//
//                                                lifecycleScope.launch {
//                                                    val data = it.first { it.id == argsId }
//
//                                                    if (data.attachmentAndOthers?.pinned == true) {
//                                                        fNTNPinButton.isSelected = true
//                                                    } else if (data.attachmentAndOthers?.pinned == false) {
//                                                        fNTNPinButton.isSelected = false
//
//                                                    }
//                                                    block(data)
//
//
//                                                }
//                                            }
//
//
//                                        }
//                                        is Result.FAILED -> {
//                                        }
//                                        is Result.LOADING -> {
//                                        }
//                                        is Result.NULL_VALUE -> {
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        //update
//
//
//                    }
//

    }

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
