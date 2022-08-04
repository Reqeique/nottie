package com.ultraone.nottie.fragment.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ultraone.nottie.adapter.NoteCollectionsAdapter
import com.ultraone.nottie.databinding.FragmentMainCollectionBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainCollectionFragment: Fragment() {
    private val collectionAdapter by lazy {
        NoteCollectionsAdapter()
    }
    private val cacheCollections: MutableList<NoteCollections> = mutableListOf()
    private val dataProvider: DataProviderViewModel by activityViewModels()
    lateinit var binding: FragmentMainCollectionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainCollectionBinding.inflate(inflater,container, false)
        lifecycleScope.launch(Main){
            binding.fMCRV.adapter = collectionAdapter
            observeCollections()
            handleSearchButton()
            handleBackButton()
        }
        return binding.root
    }
    private fun handleSearchButton(){
        binding.fMCSB.setOnClickListener {
            findNavController().navigate(MainCollectionFragmentDirections.actionMainCollectionFragmentToSearchFragment(noteCollections = cacheCollections.toTypedArray()))

        }

    }
    private fun handleBackButton(){
        binding.fMCBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    private suspend fun observeCollections(){
        dataProvider.getAllCollections().observe(viewLifecycleOwner){
            when(it){
                is Result.FAILED -> {
                    Snackbar.make(binding.root, "error: ${it.throwable.localizedMessage}", Snackbar.LENGTH_SHORT).show()
                }
                is Result.LOADING -> {

                }
                is Result.NULL_VALUE -> {

                }
                is Result.SUCCESS<*> -> {
                    it.data as Flow<List<NoteCollections>>
                    lifecycleScope.launch {
                        it.data.collect { nc ->
                            cacheCollections.clear()
                            cacheCollections.addAll(nc)
                            collectionAdapter.addList(nc.filter { it.isVisible })
                        }
                    }
                }
            }
        }
    }
}