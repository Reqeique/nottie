package com.ultraone.nottie.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.ultraone.nottie.R
import com.ultraone.nottie.adapter.MainNoteAdapter
import com.ultraone.nottie.adapter.NoteCollectionsAdapter
import com.ultraone.nottie.databinding.FragmentSearchBinding
import com.ultraone.nottie.util.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    val args: SearchFragmentArgs by navArgs()
    val noteAdapter by lazy {
        MainNoteAdapter()
    }
    val collectionAdapter by lazy {
        NoteCollectionsAdapter()
    }


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


                fSNRV.adapter = noteAdapter
                fSCRV.adapter = collectionAdapter

                setRecyclerClickListener()
                binding.setUpViews()

               searBarListener()
                setSearchBarClickListener()
            }

        }
        return binding.root
    }
    private fun setRecyclerClickListener(){
        noteAdapter.onItemClick =  {n, i, v ->
            v as MaterialCardView
            val extras = FragmentNavigatorExtras(v to "createNewNote")
            findNavController().navigate(
                SearchFragmentDirections.actionSearchFragmentToNoteTakingFragment(
                i,
                n.id,
                n),
                extras
            )

        }
        collectionAdapter.onItemClick = {nC, p, v ->
//            v as MaterialCardView
       //     val extras = FragmentNavigatorExtras(v to "createNewNote")
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToNoteCollectionNoteFragment(p,nC.id,nC)                                )
        }
    }
    private fun setSearchBarClickListener(){
        binding.fSSB.setOnCloseListener {

            exitTransition = MaterialElevationScale(true).apply{
                duration = 300
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = 300
            }
            val extras = FragmentNavigatorExtras(binding.fSSB to "fragmentSearchRootTransition")

            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToMainFragment(), extras)

            false
        }
    }
    private fun FragmentSearchBinding.setUpViews() {
        binding.chip3.setGone()
        fSC1.setGone()
        fSC2.setGone()
        chip4.setGone()
        chip5.setGone()
        fSSB.apply {
            setIconifiedByDefault(true)
            isFocusable = true
            isIconified = false

            requestFocusFromTouch()

        }
    }

    private fun searBarListener(){
        binding.fSSB.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {


                //.filter { it.filter { it.mainNote.contains(query) == true } }
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                when {
                    args.noteCollections != null && args.notes == null && args.noteCollection == null -> {
                        if (newText == null) return true
                        collectionAdapter.addList(args.noteCollections!!.filterNot { it.deleted }.filter { it.collectionName.lowercase().contains(newText) }.distinctBy { it})
                        binding.fSC2.setGone()
                        binding.fSC1.setVisible()
                        binding.chip5.setVisible()
                        binding.chip4.setGone()
                        binding.chip5.setOnCloseIconClickListener {
                            it.setGone()
                        }
                    }
                    args.notes != null && args.noteCollections == null && args.noteCollection == null-> /** [main.MainNoteFragment]*/{
                        if(newText == null) return true

                        noteAdapter.addList(args.notes!!.filterNot { it.deleted == true }.filter { it.mainNote?.lowercase()?.contains(newText.lowercase()) == true || it.title?.lowercase()?.contains(newText.lowercase()) == true}.distinctBy { it })
                        binding {
                            fSC1.setGone()
                            fSC2.setVisible()
                        }
                        binding.chip5.setGone()
                        binding.chip4.setVisible()
                        binding.chip4.setOnCloseIconClickListener {
                            it.setGone()
                        }

                    }
                    args.notes != null && args.noteCollections != null && args.noteCollection == null /** [main.MainFragment]*/-> {
                        if(newText == null) return true
                        noteAdapter.addList(args.notes!!.filterNot { it.deleted == true }.filter { it.mainNote?.lowercase()?.contains(newText.lowercase()) == true || it.title?.lowercase()?.contains(newText.lowercase()) == true}.distinctBy { it })
                        noteAdapter.notifyDataSetChanged()
                       collectionAdapter.addList(args.noteCollections!!.filterNot { it.deleted }.filter { it.collectionName.contains(newText) }.distinctBy { it})
                        binding {
                            fSC2.setVisible()
                            fSC1.setVisible()
                            chip5.setVisible()
                            chip4.setVisible()
                        }
//                        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
//                            when {
//                                checkedIds.all { it == 1} -> {
//                                    Log.d(this::class.simpleName, group.toString())
//                                }
//                                checkedIds.all { it == 2} -> {
//                                    Log.d(this::class.simpleName, group.toString())
//                                }
//                                checkedIds.containsAll(listOf(1,2)) -> {
//                                    Log.d(this::class.simpleName, group.toString())
//                                }
//                            }
//                        }
                        Log.d(this::class.qualifiedName, "${args.notes!!.filterNot { it.deleted == true}.filter { it.mainNote?.contains(newText) == true || it.title?.contains(newText)== true}.distinctBy{it}}")

//                        binding.chip5.setOnCheckedChangeListener { _, p1 ->
//                            when (p1) {
//                                true -> {
//                                    binding.fSC1.setVisible()
//                                    binding.fSC2.setGone()
//                                }
//                                false -> {
//                                    binding.fSC1.setGone()
//                                    binding.fSC2.setVisible()
//                                }
//
//                            }
//                        }
//                        binding.chip4.setOnCheckedChangeListener { _, b ->
//                            when(b){
//                                true -> {
//                                    binding.fSC1.setGone()
//                                    binding.fSC2.setVisible()
//                                }
//                                false -> {
//                                    binding.fSC1.setVisible()
//                                    binding.fSC2.setGone()
//                                }
//                            }
//                        }
//
                    }
                    args.notes != null && args.noteCollection != null  && args.noteCollections == null  -> /** [NoteCollectionNoteFragment] */ {
                        if(newText == null) return true
                        noteAdapter.addList(args.notes!!.filterNot { it.deleted == true}.filter { it.attachmentAndOthers!!.collectionId == args.noteCollection!!.id}.filter{ it.mainNote?.contains(newText) == true || it.title?.contains(newText) == true }.distinctBy { it })
                        Log.d(this@SearchFragment::class.simpleName, "T = ${args}")

                        binding {
                            fSC2.setVisible()
                            chip3.setVisible()
                            chip3.text = args.noteCollection!!.collectionName
                        }
                        binding.chip3.setOnCloseIconClickListener { it as Chip
                            it.setGone()
                            noteAdapter.addList(args.notes!!.filterNot { it.deleted == true}.filter { it.mainNote?.contains(newText) == true|| it.title?.contains(newText) == true}.distinct())
                            noteAdapter.notifyDataSetChanged()
                        }
                    }

                }


                return false
            }

        })
    }
    override fun onDetach() {
        super.onDetach()
        //TODO Implement progressbar with different color
        //TODO Implement archive support
        //TODO Implement tag support
        //TODO Implement graphics support
        //note.removeAll { true }
    }
}
