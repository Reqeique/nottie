package com.ultraone.nottie.fragment.archive

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.ultraone.nottie.R
import com.ultraone.nottie.adapter.MainNoteAdapter
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.databinding.FragmentMainArchiveBinding
import com.ultraone.nottie.fragment.notetaking.NoteTakingFragment
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.util.NULL_VALUE_INT
import com.ultraone.nottie.util.resolver
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import com.ultraone.nottie.viewmodel.NoteTakingFragmentViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainArchiveFragment: Fragment() {
    private val noteAdapter by lazy {
        MainNoteAdapter()
    }
    private val noteTakingFragmentViewModel: NoteTakingFragmentViewModel by activityViewModels()
    private val dataProviderViewModel: DataProviderViewModel by activityViewModels()
    lateinit var binding: FragmentMainArchiveBinding
    private var cacheNote: MutableList<Note> = mutableListOf()
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
        toNoteTakingFragment()
//        registerForActivityResult(ActivityResultContracts.GetContent()) {
//            Log.d("${NoteTakingFragment.TAG}@110", "URI = $it")
//        }


    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainArchiveBinding.inflate(inflater, container, false)
        lifecycleScope.launchWhenCreated {
            binding.fMARecycler.adapter = noteAdapter
            observeNote()
        }
        return binding.root
    }
    private fun observeNote(){
    lifecycleScope.launch {
        dataProviderViewModel.getAllNotes().observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                when(it){
                    is Result.FAILED -> {
                        Snackbar.make(binding.root, "error: ${it.throwable.localizedMessage}", Snackbar.LENGTH_SHORT).show()
                    }
                    is Result.LOADING -> {

                    }
                    is Result.NULL_VALUE -> {

                    }
                    is Result.SUCCESS<*> -> {
                        it.data as Flow<List<Note>>
                        it.data.collect {
                            noteAdapter.addList(it.filter { it.attachmentAndOthers?.archived == true })
                            cacheNote.clear()
                            cacheNote.addAll(it)
                        }
                    }

                }
            }
        }
    }
    }
    private fun toNoteTakingFragment(){
        noteAdapter.onItemClick = { n, i, v ->
            findNavController().navigate(MainArchiveFragmentDirections.actionMainArchiveFragmentToNoteTakingFragment(i, n.id, n))
        }
        binding.fMAFab.setOnClickListener {
            findNavController().navigate(MainArchiveFragmentDirections.actionMainArchiveFragmentToNoteTakingFragment(NULL_VALUE_INT, NULL_VALUE_INT))

        }

    }

}