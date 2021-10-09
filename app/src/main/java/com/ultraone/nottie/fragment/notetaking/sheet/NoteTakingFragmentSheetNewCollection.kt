package com.ultraone.nottie.fragment.notetaking.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultraone.nottie.adapter.NoteTakingSheetCollectionAdapter
import com.ultraone.nottie.databinding.FragmentNoteTakingNewBinding
import com.ultraone.nottie.databinding.FragmentNoteTakingSheetNewCollectionBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.filterById
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import com.ultraone.nottie.viewmodel.NoteTakingFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NoteTakingFragmentSheetNewCollection: BottomSheetDialogFragment() {
    private val dataProvider by activityViewModels<DataProviderViewModel>()
    lateinit var adapter: NoteTakingSheetCollectionAdapter
    private val noteTakingFragmentViewModel by activityViewModels<NoteTakingFragmentViewModel>()
    lateinit var binding: FragmentNoteTakingSheetNewCollectionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =FragmentNoteTakingSheetNewCollectionBinding.inflate(inflater, container, false)
        adapter = NoteTakingSheetCollectionAdapter()
        binding.fNTSNCRecycler.adapter = adapter
        lifecycleScope.launch(Main) {
            dataProvider.getAllCollections().observe(viewLifecycleOwner) {result ->
                lifecycleScope.launch {
                    when (result) {
                        is Result.LOADING -> {
                        }
                        is Result.FAILED -> {
                        }
                        is Result.NULL_VALUE -> {
                        }
                        is Result.SUCCESS<*> -> {
                            val data = result.data as Flow<List<NoteCollections>>
                            data.collect {

//                                dataProvider.getAllNotes().observe(viewLifecycleOwner) { result ->
//                                    when(result){
//                                        is Result.FAILED -> {}
//                                        is Result.LOADING -> {}
//                                        is Result.NULL_VALUE -> {}
//                                        is Result.SUCCESS<*> -> {
//                                            val noteflow = result.data as Flow<List<Note>>
//                                            lifecycleScope.launch {
//                                                noteflow.collect { notes ->
//                                                    binding.fNTSNCTitle.text = it.filterById(notes.filterById(noteTakingFragmentViewModel.collectionId.value).attachmentAndOthers.collectionId)
//
//                                                }
//                                            }
//
//                                        }
//                                    }
//                                    //filterById(noteTakingFragmentViewModel.collectionId.value!!).collectionName
//                                }
                                  if(noteTakingFragmentViewModel.collectionId.value != null) binding.fNTSNCTitle.text = it.filterById(noteTakingFragmentViewModel.collectionId.value!!).collectionName
                                  adapter.addList(it)
                            }
                        }
                    }
                }
            }

            adapter.onItemClick = { collection, pos, v ->
                lifecycleScope.launch {
                    noteTakingFragmentViewModel.collectionId.emit(collection.id)
                    dismiss()
                }
            }

        }

        return binding.root
    }
}