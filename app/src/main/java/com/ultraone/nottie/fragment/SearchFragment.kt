package com.ultraone.nottie.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.ultraone.nottie.R
import com.ultraone.nottie.adapter.MainNoteAdapter
import com.ultraone.nottie.databinding.FragmentSearchBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.invoke
import com.ultraone.nottie.util.resolver
import com.ultraone.nottie.util.toast
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    val adapter by lazy {
        MainNoteAdapter()
    }
    var note: MutableList<Note> =
        mutableListOf()

    private val dataProvider: DataProviderViewModel by activityViewModels()
    private lateinit var binding: FragmentSearchBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding {
            lifecycleScope.launch(Main) {

                fSRV.adapter = adapter
                //   adapter.addList()
                fSSB.apply {
                    setIconifiedByDefault(true)
                    isFocusable = true
                    isIconified = false

                    requestFocusFromTouch()

                }



                fSSB.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {


                        //.filter { it.filter { it.mainNote.contains(query) == true } }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        Log.d("SFKt", "Coroutine $newText")

                            Log.d("SFKt", "test")
                            if (newText == null) return true

                                adapter.addList(note.filterNot { it.deleted == true }.filter {
                                    it.mainNote?.contains(newText) == true || it.title?.contains(
                                        newText
                                    ) == true
                                }.distinctBy { it })
                               adapter.notifyDataSetChanged()


                        return false
                    }

                })
                observeNote(adapter)
                fSSB.setOnCloseListener {
                    findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToMainFragment())

                    false
                }
            }

        }
        return binding.root
    }

    private suspend fun observeNote(adapter: MainNoteAdapter) {

        dataProvider.getAllNotes().observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                when (it) {
                    is Result.FAILED -> {
                        Snackbar.make(
                            binding.root,
                            "Throwing: ${it.throwable.localizedMessage}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is Result.LOADING -> {
                        //TODO implement loading animation
                    }
                    is Result.NULL_VALUE -> {

                    }

                    is Result.SUCCESS<*> -> {
                        it.data as Flow<List<Note>>
                        it.data.collect {
                            note.addAll(it)
                            adapter.addList(it)
                        }
                    }
                }
            }
        }

    }

    override fun onDetach() {
        super.onDetach()
        note.removeAll { true }
    }
}
