package com.ultraone.nottie.fragment.main

import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import com.ultraone.nottie.R
import com.ultraone.nottie.adapter.*
import com.ultraone.nottie.databinding.FragmentMainBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.model.Snippie
import com.ultraone.nottie.util.*
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import com.ultraone.nottie.viewmodel.NoteTakingFragmentViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*


//ERROR java.lang.IllegalArgumentException: Navigation action/destination com.ultraone.nottie:id/action_noteTakingFragment_to_mainFragment cannot be found from the current destination Destination(com.ultraone.nottie:id/mainFragment) label=MainFragment class=com.ultraone.nottie.fragment.main.MainFragment
//
class MainFragment : Fragment() {
    companion object {
        const val TAG = "::MainFragment"
    }

    private var cacheNote: MutableList<Note> = mutableListOf()
    private var cacheCollection: MutableList<NoteCollections> = mutableListOf()
    private var cacheSnippie: Snippie? = null
    lateinit var collectionsAdapter: NoteCollectionsAdapter
    private val  dateTimeAdapter by lazy {
        DateTimeAdapter()
    }
    lateinit var NoteAdapter: NoteAdapter
    val dataProvider by activityViewModels<DataProviderViewModel>()
    lateinit var controller: NavController
    lateinit var config: AppBarConfiguration
    val noteTakingFragmentViewModel: NoteTakingFragmentViewModel by activityViewModels()


    private var tracker: SelectionTracker<Long>? = null
    lateinit var binding: FragmentMainBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val contextThemeWrapper = ContextThemeWrapper(requireActivity(), R.style.mainFragStyle)
        //   context?.theme?.applyStyle(R.style.mainFragStyle,false)

        requireActivity().window.apply {
            navigationBarColor =
                requireContext().resolver(R.attr.colorSurfaceTertiary)

        }
        binding = FragmentMainBinding.inflate(inflater, container, false)

        lifecycleScope.launchWhenCreated {
            Log.d(this::class.simpleName, "lifecycle called ")
            binding.fragmentMainRecyclerNote.addRecyclerListener {
                it.setIsRecyclable(false)
            }
            delay(400L)
        dateTimeAdapter
            binding.setUpNotes()
            binding.setUpCollections()
            binding.setUpSnippie()
            binding.setSearchCardClickListener()

            handleOpenArchive()
            dateTimeAdapter.generate()
            val pat = SimpleDateFormat("MMMM, dd")

            binding.textView3.text = pat.format(Date())
            binding.fragmentMainRecyclerDateTime.adapter = dateTimeAdapter



            tracker = SelectionTracker.Builder(
                "mySelection",
                binding.fragmentMainRecyclerNote,
                StableIdKeyProvider(binding.fragmentMainRecyclerNote),
                MyItemDetailLookup(binding.fragmentMainRecyclerNote),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).also {

            }.build()

            NoteAdapter.tracker = tracker
        }



        return binding.root

    }

    /**
     * [setSearchCardClickListener] used to apply transition and click listener to [FragmentMainBinding.fragmentMainSearchCard]
     */
    private fun FragmentMainBinding.setSearchCardClickListener() {
        trans()
        fragmentMainSearchCard.setOnClickListener { view ->
            val name = "fragmentSearchRootTransition"
            val extras = FragmentNavigatorExtras(view to name)
            val op = NavOptions.Builder().setRestoreState(true)

            //  findNavController().popBackStack(R.id.action_mainFragment_to_searchFragment, false)
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToSearchFragment(
                    cacheNote.toTypedArray(),
                    noteCollections = cacheCollection.toTypedArray()
                ),
                extras
            )

        }
    }

    /**
     *  those set of function used to invoke changes on the child of note site or so called `binding.fragmentMainRootNote` based on the condition
     *  */
    private suspend fun FragmentMainBinding.setUpNotes() {
        NoteAdapter = NoteAdapter()
        fragmentMainRecyclerNote.adapter = NoteAdapter
        observeNote()
        setNoteAddListener()
        setNoteRecyclerItemClickListener()
        setNoteGoClickListener()

    }

    private fun handleMyChip(nc: Chip, pc: Chip, sc: Chip?, it: List<Note>, adapter: NoteAdapter){
        handleFilters(
            nc, pc, sc
        ) { n, p, s ->
            when {
                n -> {
                    val filtered = it.filter { note ->
                        note.dateTime?.substring(0..7) == currentTime.substring(0..7)
                    }
//
                    adapter.addList(filtered)
                }
                p -> {
                    val filtered = it.filter { note ->
                        note.deleted == false && note.attachmentAndOthers?.archived == false
                    }.filter { it.attachmentAndOthers?.pinned == true }

                    adapter.addList(filtered)

                }

                else -> {
                    val filtered = it.filter { note ->
                        note.deleted == false && note.attachmentAndOthers?.archived == false
                    }

                    adapter.addList(filtered)
                    //   Log.d("$TAG@198", "$isChecked + $filtered")
                }
            }
        }
    }

    private fun FragmentMainBinding.setNoteAddListener() {
        fragmentMainAddNote.setOnClickListener {
            it as ImageButton
//                    val name = "createNewNote"
//                    val extras = FragmentNavigatorExtras(it to name)
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToNoteTakingFragment(
                    NULL_VALUE_INT,
                    NULL_VALUE_INT
                )
            )

        }

    }

    private fun FragmentMainBinding.setNoteGoClickListener() {
        fragmentMainOpenNote.setOnClickListener {
            it as ImageButton
//            val name = "createNewNote"
//            val extras = FragmentNavigatorExtras(it to name)
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToMainNoteFragment())
        }
    }

    private fun setNoteRecyclerItemClickListener() {

        trans()
        NoteAdapter.onItemClick = { note, pos, view ->
            view as MaterialCardView
            val name = "createNewNote"
            Log.d("$TAG@95", "- ${view.transitionName}")


//            findNavController().navigate(
//                MainFragmentDirections.actionMainFragmentToSearchFragment()
//            )
            val extras = FragmentNavigatorExtras(view to name)

            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToNoteTakingFragment(
                    pos,
                    note.id,
                    note
                ), extras
            )


        }
    }

    private suspend fun observeNote() {
        dataProvider.getAllNotes().observe(viewLifecycleOwner) {
            lifecycleScope.launch(Main) {
                when (it) {
                    is Result.SUCCESS<*> -> {
                        it.data as Flow<List<Note>>
                        it.data.collect { datas ->
                            handleMyChip(binding.chip5, binding.chip4,null, datas, NoteAdapter)
                            cacheNote.clear()
                            cacheNote.addAll(datas.filter { it.deleted == false && it.attachmentAndOthers?.archived == false })
                            NoteAdapter.addList(datas.filter {
                                it.deleted == false && it.attachmentAndOthers?.archived == false
                            })
                        }
                        dataProvider.getAllNotes().removeObservers(viewLifecycleOwner)


                    }
                    is Result.FAILED -> TODO()
                    Result.LOADING -> TODO()
                    Result.NULL_VALUE -> TODO()
                }
            }
        }
    }
    /***/


    /**
     *  those set of function used to invoke changes on the child of collection site or so called `binding.fragmentMainRootCollection` based on the condition
     *  */
    private suspend fun FragmentMainBinding.setUpCollections() {


        collectionsAdapter = NoteCollectionsAdapter()
        fragmentMainRecyclerCollection.adapter = collectionsAdapter
        observeNoteCollection()
        setNoteCollectionRecyclerItemClickListener()
        setCollectionAddListener()
        handleOpenCollection()
    }

    private fun setNoteCollectionRecyclerItemClickListener() {
        trans()
        collectionsAdapter.onItemClick = { noteCollection, pos, v ->
            val extras = FragmentNavigatorExtras(v to "createNewCollectionf")
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToNoteCollectionNoteFragment(
                    pos,
                    noteCollection.id,
                    noteCollection
                ), extras
            )

        }
    }

    private suspend fun observeNoteCollection() {
        dataProvider.getAllCollections().observe(viewLifecycleOwner) {
            lifecycleScope.launch(Main) {
                when (it) {
                    is Result.FAILED -> {

                    }
                    is Result.LOADING -> {

                    }
                    is Result.NULL_VALUE -> {

                    }
                    is Result.SUCCESS<*> -> {
                        it.data as Flow<List<NoteCollections>>
                        it.data.collect { datas ->

                            if (datas.firstOrNull {
                                    it.collectionName == "Untitled Collection" && !it.isVisible
                                } == null) {
                                dataProvider.addCollection(
                                    NoteCollections(
                                        0,
                                        "Untitled Collection",
                                        false,
                                        isVisible = false
                                    , currentTime)
                                ).observe(viewLifecycleOwner)
                            }
                            cacheCollection.clear()
                            cacheCollection.addAll(datas.filterNot { it.deleted  })
                            collectionsAdapter.addList(datas.filter { it.isVisible }
                                .filterNot { it.deleted }, cacheNote)
                        }
                    }
                }
            }
        }
    }

    private fun handleOpenCollection() {
        binding.fragmentMainOpenCollection.setOnClickListener {
            val anim = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
                duration = 300L
            }
            reenterTransition = anim

            exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                duration = 300L
            }
            enterTransition = anim
            val extras = FragmentNavigatorExtras(binding.root to "f_m_c_t")
            findNavController().apply {
                (this@apply).saveState()
            }.navigate(MainFragmentDirections.actionMainFragmentToMainCollectionFragment(), extras)
        }
    }

    private fun handleOpenArchive() {
        binding.fragmentMainOpenArchive.setOnClickListener {
            trans()
            val extras = FragmentNavigatorExtras(it to "f_m_a_r_t")
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToMainArchiveFragment(),
                extras
            )
        }
    }


    private fun FragmentMainBinding.setCollectionAddListener() {
        fragmentMainAddCollection.setOnClickListener {
            requireContext().dialog({
                requestWindowFeature(Window.FEATURE_NO_TITLE)
            }, R.layout.fragment_main_add_collection_dialog) {

                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                this@dialog.findViewById<EditText>(R.id.fragment_main_add_collection_dialog_collection_name)
                    .doOnTextChanged { text, start, before, count ->
                        if (!text.isNullOrEmpty()) {
                            this@dialog.findViewById<TextView>(R.id.fragment_main_add_collection_dialog_add_button)
                                .setOnClickListener {
                                    lifecycleScope.launch {
                                        dataProvider.addCollection(
                                            NoteCollections(
                                                0,
                                                text.toString(),
                                                deleted = false,
                                                isVisible = true
                                            , currentTime)
                                        ).observe(viewLifecycleOwner) {
                                            when (it) {
                                                is Result.FAILED -> {
                                                }
                                                is Result.LOADING -> {
                                                }
                                                is Result.NULL_VALUE -> {
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

            }
        }
    }


    private suspend fun FragmentMainBinding.setUpSnippie() {
        observeSnippie()
        setSnippieChangeListener()
    }

    private fun setSnippieChangeListener() {
        lifecycleScope.launch {
                    val job1 = launch {
                        dataProvider.getSnippie.asFlow().collect {
                            binding.fragmentMainSnippie.text = it.mainNote.toString().toEditable()
                            Log.d("MFKt@395", "$it")
                        }
                    }

                    delay(100)
                    job1.cancel()


                    // = dataProvider.getSnippie.value.toString().toEditable()


        }
        binding.fragmentMainSnippie.doOnTextChanged { text, start, before, count ->
            if(binding.fragmentMainSnippie.hasFocus()) {

                lifecycleScope.launch {
                    val snippie = Snippie(0, text.toString(), dateTime = currentTime)
                    if (cacheSnippie == null) {
                        Log.d("MFkt@376", "text =$text , snippie = $snippie")
                        dataProvider.addSnippie(
                            snippie
                        ).observe(viewLifecycleOwner)

                    } else {
                        dataProvider.updateSnippie(
                            cacheSnippie!!.copy(dateTime = currentTime, mainNote = text.toString())
                        ).observe(viewLifecycleOwner)
                    }
                }
            }

        }
    }

    private suspend fun observeSnippie() {
        dataProvider.getSnippie().observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                when (it) {
                    is Result.FAILED -> {

                    }
                    Result.LOADING -> {

                    }
                    Result.NULL_VALUE -> {

                    }
                    is Result.SUCCESS<*> -> {
                        it.data as Flow<Snippie?>

                        it.data?.collect {
                            if(it == null) return@collect
                            Log.d("MKt@407", it.toString())
                            cacheSnippie = it
                            dataProvider.getSnippie.setValue(it)
                    //        binding.fragmentMainSnippie.text = it.mainNote.toEditable()
                             dataProvider.getSnippie().removeObservers(viewLifecycleOwner)
                            //binding.fragmentMainSnippie.text = cacheSnippie!!.mainNote.toEditable()
                        }
                    }
                }
            }
        }
    }


    /***/
    override fun onDetach() {
        super.onDetach()
        requireActivity().window.navigationBarColor = requireContext().resolver(R.attr.colorSurface)
        lifecycleScope.cancel()
        Log.i("$TAG@59", "Detached")
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().window.navigationBarColor = requireContext().resolver(R.attr.colorSurface)
        Log.i("$TAG@65", "Destroyed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.navigationBarColor = requireContext().resolver(R.attr.colorSurface)
        Log.i("$TAG@71", "DestroyedView")
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onPause() {
        super.onPause()
        lifecycleScope
        Log.i("$TAG@76", "Paused")
    }

    private fun trans() {
        MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration = 300L
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 300L
        }
        exitTransition = MaterialElevationScale(false).apply {
            duration = 300L
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = 300L
        }
    }
}