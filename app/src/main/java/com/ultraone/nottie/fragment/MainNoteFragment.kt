package com.ultraone.nottie.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import com.ultraone.nottie.adapter.MainNoteAdapter
import com.ultraone.nottie.databinding.FragmentMainNoteBinding

import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.invoke
import com.ultraone.nottie.util.shortSnackBar
import com.ultraone.nottie.util.toast
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import com.ultraone.nottie.viewmodel.NoteTakingFragmentViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect


class MainNoteFragment : Fragment() {
    companion object {
        const val TAG = "::MainNoteFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  reenterTransition = MaterialSharedAxis()

    }
    private lateinit var changingTitle : String
    private val dataProvider: DataProviderViewModel by activityViewModels()
    private lateinit var dataClient: List<Note>
    private lateinit var adapter: MainNoteAdapter
    private lateinit var binding: FragmentMainNoteBinding


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainNoteBinding.inflate(inflater, container, false)
        binding {

            lifecycleScope.launch {

                adapter = MainNoteAdapter()
                fragmentMainNoteRecycler.adapter = adapter
                setClickListener()
                onSwipeHandler()
                observeNote(adapter)
                recyclerClickListener(adapter)
            }

        }

        return binding.root
    }

    private fun recyclerClickListener(adapter: MainNoteAdapter) {
        adapter.onItemClick = { note, pos, _ ->
            findNavController().navigate(
                MainNoteFragmentDirections.actionMainNoteFragmentToNoteTakingFragment(
                    pos,
                    note.id
                )
            )
        }
    }

    private fun FragmentMainNoteBinding.setClickListener() {
        fMNFab.setOnClickListener {
            enterTransition = MaterialElevationScale(true).apply {
                duration = 300

            }
            val extras = FragmentNavigatorExtras(fMNFab to "createNewNote")
            findNavController().navigate(
                MainNoteFragmentDirections.actionMainNoteFragmentToNoteTakingFragment(),
                extras
            )

        }
    }

    private fun onSwipeHandler() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN
        ) {

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                toast("Are you sure", true)

                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                lifecycleScope.launch {
                    dataClient[viewHolder.adapterPosition].let {
                        dataProvider.deleteNote(it).observe(viewLifecycleOwner){ it ->
                            when(it){
                               is Result.LOADING -> {

                               }
                                is Result.FAILED -> TODO()
                                is Result.NULL_VALUE -> TODO()
                                is Result.SUCCESS<*> -> {
                                    binding.root.shortSnackBar("Deleted Successfully")
                                }
                            }
                        }
                    }
                    Log.d("$TAG@144", "${viewHolder.adapterPosition} + $dataClient")
                }


            }

        }).attachToRecyclerView(binding.fragmentMainNoteRecycler)

    }


    private fun observeNote(adapter: MainNoteAdapter) {
        lifecycleScope.launch {
            dataProvider.getAllNotes().observe(viewLifecycleOwner) {
                lifecycleScope.launch(Main) {

                    when (it) {
                        is Result.FAILED -> {
                            toast("FAILED ${it.throwable.localizedMessage}")
                        }
                        is Result.LOADING -> TODO()
                        is Result.NULL_VALUE -> TODO()
                        is Result.SUCCESS<*> -> {
                            toast("Sounds like coroutine is working")
                            it.data as Flow<*>
                            it.data.collect {
                                it as List<Note>

                                binding.fMNChipDefault.setOnCheckedChangeListener { view, isChecked ->

                                    if (isChecked) {

                                        val filtered = it.filter { note ->
                                            note.deleted == false
                                        }.sortedBy { data -> data.title }
                                        Log.d("$TAG@185", "$isChecked + $filtered")
                                        dataClient = filtered
                                        adapter.addList(filtered)
                                    } else (!isChecked) {
                                        val filtered = it.filter { note ->
                                            note.deleted == false
                                        }
                                        dataClient = filtered
                                        adapter.addList(filtered)
                                        Log.d("$TAG@198", "$isChecked + $filtered")
                                    }
                                }
                                val filtered = it.filter { note ->
                                    note.deleted == false
                                }
                                dataClient = filtered
                                adapter.addList(filtered)


                            }
                        }
                    }
                }
            }
        }
    }

    fun addNote(it: Result) {

    }
}