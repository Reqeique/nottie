package com.ultraone.nottie.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialElevationScale
import com.ultraone.nottie.adapter.MainNoteAdapter
import com.ultraone.nottie.databinding.FragmentNoteCollectionNoteBinding
import com.ultraone.nottie.fragment.main.MainNoteFragmentDirections
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
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

                                cacheCollection =  it1.first { c -> c.id == args.id}
                            }
                        }
                    }
                }
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
        }
        return binding.root
    }
}