package com.ultraone.nottie.fragment

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.discord.simpleast.core.simple.SimpleRenderer
import com.google.android.material.snackbar.Snackbar
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.concurrent.Executors
import kotlin.contracts.ExperimentalContracts

class NoteTakingFragment : Fragment() {
    companion object {

        const val TAG = "::NoteTakingF"
    }
   private lateinit var notes: List<Note>
    private lateinit var attachmentAndOther: NoteAttachmentAndOther
     private lateinit var binding: FragmentNoteTakingNewBinding
    private val noteTakingFragmentViewModel: NoteTakingFragmentViewModel by activityViewModels()
    private val dataProvider: DataProviderViewModel by activityViewModels()
    private val args: NoteTakingFragmentArgs by navArgs()
    private fun setupMarkdown() {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycleScope.launch(Main){


        }
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




    }

//    lateinit var note: Note

    @ExperimentalContracts
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteTakingNewBinding.inflate(inflater, container, false)

        binding {
//            requireActivity().window.navigationBarColor = ContextCompat.getColor(requireActivity(), R.color.gray_00)

            lifecycleScope.launch(Main) {
                setUpNoteId()

                configureAddButton()
                fNTNBackButton.setOnClickListener {
                    findNavController().let {
                        it.navigate(NoteTakingFragmentDirections.actionNoteTakingFragmentToMainFragment())
                        it.popBackStack()

                    }
                }



               val _note = args.note
                fNTNNotes.text = _note?.mainNote?.toEditable()
                fNTNTitle.text = _note?.title?.toEditable()


                observeNoteFirst { note ->
                         fNTNTitle.doOnTextChanged { text, _, _, _ ->
                             if (!text.isNullOrBlank()) {
                                 lifecycleScope.launch(Main) {
                                     dataProvider.updateOrCreateNewNote(note.copy(title = text.toString())).observe(viewLifecycleOwner){
                                         Log.d("$TAG@129", it.data)
                                     }
                                 }
                               //  toast("note.id.toString" + note.id.toString())
//                                   noteTakingFragmentViewModel.noteId.observe(viewLifecycleOwner) {
//                                       if (it != NULL_VALUE_INT) {
//                                           update(
//                                               note.copy(title = text.toString())
//                                           )
//                                       } else if (it == NULL_VALUE_INT) {
//                                           //   toast("text @130"+"$text")
//
//                                           new(
//                                               note.copy(title = text.toString())
//                                           )
//                                       }
//                                   }
                             }


                         }

                         fNTNNotes.doOnTextChanged { changingText, _, _, _ ->
                             if (!changingText.isNullOrEmpty()) {
                                 fNTNBackButton.visibility = View.GONE
                                 fNTNSaveButton.visibility = View.VISIBLE
                                 update(
                                     note.copy(mainNote = changingText.toString())
                                 )


                             } else if (changingText.isNullOrEmpty()) {
                                 fNTNBackButton.visibility = View.VISIBLE
                                 fNTNSaveButton.visibility = View.GONE
                             }
                         }

                         fNTNPinButton.invokeSelectableState<ImageButton> {
                             binding.root.shortSnackBar(it.toString())

                             update(
                                 note.copy(attachmentAndOthers = attachmentAndOther.copy(pinned = it))
                             )

                         }

                }





            }

        }

        return binding.root
    }

    private fun setUpNoteId() {
        noteTakingFragmentViewModel.noteId.value = args.id
    }
    /**function [observeNoteFirst] used to observe for the note that has been declared before or return new note*/
    private suspend fun observeNoteFirst(block: (Note) -> Unit) = with(binding){
        noteTakingFragmentViewModel.noteId.observe(viewLifecycleOwner) { argsId ->
            val note = Note(
                0,
                null,
                null,
                null,
                 null,
                false
            )
            if (argsId == NULL_VALUE_INT) {
               block(note)


            }
            else (argsId != NULL_VALUE_INT){
                lifecycleScope.launch {

                    dataProvider.getAllNotes().observe(viewLifecycleOwner) {
                        lifecycleScope.launch(Main) {
                            when (it) {

                                is Result.SUCCESS<*> -> {
                                    val data = it.data as Flow<List<Note>>

                                   data.collect {

                                        lifecycleScope.launch {
                                            val data = it.first { it.id == argsId }

                                            if (data.attachmentAndOthers?.pinned == true) {
                                                fNTNPinButton.isSelected = true
                                            } else if (data.attachmentAndOthers?.pinned == false) {
                                                fNTNPinButton.isSelected = false

                                            }
                                            block(data)


                                        }
                                    }


                                }
                                is Result.FAILED -> {
                                }
                                is Result.LOADING -> {
                                }
                                is Result.NULL_VALUE -> {
                                }
                            }
                        }
                    }
                }
                //update


            }
        }
    }

    private fun FragmentNoteTakingNewBinding.new(note: Note) = with(lifecycleScope){
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
                                noteTakingFragmentViewModel.noteId.postValue(data.toInt())
                            }

                        }
                    }
                }
           // }
        }
    }
    private fun FragmentNoteTakingNewBinding.update(note: Note) = with(lifecycleScope) {
         launch {
             noteTakingFragmentViewModel.noteId.observe(viewLifecycleOwner) {argsId ->
                 lifecycleScope.launch {
                     if (argsId != NULL_VALUE_INT) {
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
         }



    }







     /**[configureAddButton] is used to invoke click  listener to [FragmentNoteTakingNewBinding.fNTNAddButton]*/
    private fun FragmentNoteTakingNewBinding.configureAddButton(){
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
