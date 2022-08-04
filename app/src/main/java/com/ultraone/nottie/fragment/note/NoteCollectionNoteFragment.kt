package com.ultraone.nottie.fragment.note

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.ultraone.nottie.R
import com.ultraone.nottie.adapter.MainNoteAdapter
import com.ultraone.nottie.databinding.FragmentNoteCollectionNoteBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.dialog
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NoteCollectionNoteFragment : Fragment() {
    private val cacheNotes: MutableList<Note> =mutableListOf()
    lateinit var cacheCollection: NoteCollections
    private lateinit var adapter: MainNoteAdapter
    private lateinit var binding: FragmentNoteCollectionNoteBinding
    private val dataProvider: DataProviderViewModel by viewModels()
    private val args: NoteCollectionNoteFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteCollectionNoteBinding.inflate(inflater, container, false)
        lifecycleScope.launch(Main) {
            adapter = MainNoteAdapter()
            binding.fNCNRecycler.adapter = adapter
            binding.fNCNCollectionTitle.text = args.noteCollections?.collectionName
            setRecyclerClickListener()
            dataProvider.getAllNotes().observe(viewLifecycleOwner) {
                  when(it){
                      is Result.FAILED -> {

                      }
                      is Result.LOADING -> {

                      }
                      is Result.NULL_VALUE -> {

                      }
                      is Result.SUCCESS<*> -> {
                          val notesFlow = it.data as Flow<List<Note>>
                          lifecycleScope.launch {
                              notesFlow.collect { notes ->
                                  val note = notes.filter { it1 ->
                                      it1.attachmentAndOthers?.collectionId == args.id
                                  }

                                  cacheNotes.clear()
                                  cacheNotes.addAll(notes)
                                  adapter.addList(note)

                              }
                          }
                      }
                  }
            }
            dataProvider.getAllCollections().observe(viewLifecycleOwner) {
                when(it){
                    is Result.FAILED -> {

                    }
                    Result.LOADING ->  {

                    }
                    Result.NULL_VALUE -> {

                    }
                    is Result.SUCCESS<*> -> {
                        it.data as Flow<List<NoteCollections>>
                        lifecycleScope.launch {
                            it.data.collect {it1 ->
                                binding.fNCNCollectionTitle.text = it1.first { c -> c.id == args.id }.collectionName
                                cacheCollection =  it1.first { c -> c.id == args.id}
                            }
                        }
                    }
                }
            }
            binding.fNCNEB.setOnClickListener {
                requireContext().dialog({
                           requestWindowFeature(Window.FEATURE_NO_TITLE)
                }, R.layout.fragment_main_add_collection_dialog,{
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    findViewById<EditText>(R.id.fragment_main_add_collection_dialog_collection_name).apply { setText(cacheCollection.collectionName) }.doOnTextChanged { text, start, before, count ->
                        if(!text.isNullOrEmpty()){

                           findViewById<TextView>(R.id.fragment_main_add_collection_dialog_add_button).apply { setText("Edit")}.setOnClickListener { it as TextView

                                   lifecycleScope.launch {
                                       dataProvider.updateCollections(
                                           cacheCollection.copy(
                                               collectionName = text.toString()
                                           )
                                       ).observe(viewLifecycleOwner){
                                           when(it){
                                               is Result.FAILED -> {
                                                   Snackbar.make(binding.root, "message: ${it.throwable.localizedMessage}", Snackbar.LENGTH_SHORT).show()
                                               }
                                               Result.LOADING -> {

                                               }
                                               Result.NULL_VALUE -> {

                                               }
                                               is Result.SUCCESS<*> -> {
                                                   dismiss()
                                               }
                                           }
                                       }
                                   }

                           }
                        }
                    }
                    show()
                })
            }
            binding.fNCNS.setOnClickListener {
                if(!::cacheCollection.isInitialized) return@setOnClickListener
                enterTransition = MaterialElevationScale(true).apply{
                    duration = 300
                }
                val extras = FragmentNavigatorExtras(binding.fNCNS to "test")
                findNavController().navigate(
                    NoteCollectionNoteFragmentDirections.actionNoteCollectionNoteFragmentToSearchFragment2(cacheNotes.toTypedArray(), cacheCollection),
                    extras
                )
                Log.d(this@NoteCollectionNoteFragment::class.simpleName, """
                    |onCreateView: cacheC $cacheCollection 
                    |
                    |cacheN $cacheNotes
                    |""".trimMargin())
            }
            handleBackButton()
        }
        return binding.root
    }
    private fun setRecyclerClickListener(){
        adapter.onItemClick =  {n, i, v ->
            v as MaterialCardView
            val extras = FragmentNavigatorExtras(v to "createNewNote")
            findNavController().navigate(
                NoteCollectionNoteFragmentDirections.actionNoteCollectionNoteFragmentToNoteTakingFragment(//.actionSearchFragmentToNoteTakingFragment(
                    i,
                    n.id,
                    n),
                extras
            )

        }
    }
    private fun handleBackButton(){
        binding.fNCNBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}