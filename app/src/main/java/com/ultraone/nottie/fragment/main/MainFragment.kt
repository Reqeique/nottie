package com.ultraone.nottie.fragment.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.transition.MaterialElevationScale
import com.ultraone.nottie.R
import com.ultraone.nottie.adapter.DateTimeAdapter
import com.ultraone.nottie.adapter.MyItemDetailLookup
import com.ultraone.nottie.adapter.NoteAdapter
import com.ultraone.nottie.adapter.NoteCollectionsAdapter
import com.ultraone.nottie.databinding.FragmentMainBinding
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.NULL_VALUE_INT
import com.ultraone.nottie.util.dialog
import com.ultraone.nottie.util.invoke
import com.ultraone.nottie.util.resolver
import com.ultraone.nottie.viewmodel.DataProviderViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


//ERROR java.lang.IllegalArgumentException: Navigation action/destination com.ultraone.nottie:id/action_noteTakingFragment_to_mainFragment cannot be found from the current destination Destination(com.ultraone.nottie:id/mainFragment) label=MainFragment class=com.ultraone.nottie.fragment.main.MainFragment
//
class MainFragment : Fragment() {
    companion object {
        const val TAG = "::MainFragment"
    }
    lateinit var collectionsAdapter: NoteCollectionsAdapter
    lateinit var dateTimeAdapter: DateTimeAdapter
    lateinit var NoteAdapter: NoteAdapter
    val dataProvider by activityViewModels<DataProviderViewModel>()
    lateinit var controller: NavController
    lateinit var config: AppBarConfiguration
    private val month = 1..31

    private var tracker: SelectionTracker<Long>? = null
    lateinit var binding: FragmentMainBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val contextThemeWrapper = ContextThemeWrapper(requireActivity(), R.style.mainFragStyle)
        //   context?.theme?.applyStyle(R.style.mainFragStyle,false)
        requireActivity().window.navigationBarColor =
            requireContext().resolver(R.attr.colorOnSecondary)
        binding = FragmentMainBinding.inflate(inflater, container, false)
//        controller = childFragmentManager.findFragmentById(R.id.fragment_container_viewer)!!.findNavController()
//        binding.bottomNavigationView2.setupWithNavController(controller)
//        config = AppBarConfiguration(
//            setOf(R.id.fragment_main_note_root, R.id.fragment_main_account_root)
//        )
//
//        //findNavController().navigate(MainFragmentDirections.actionMainFragmentToNoteTakingFragment())
//        val direction = MainFragmentDirections
        binding {
            lifecycleScope.launch(Main) {

                setUpNotes()
                setUpCollections()
                setSearchCardClickListener()
                dateTimeAdapter = DateTimeAdapter()

                fragmentMainRecyclerDateTime.adapter = dateTimeAdapter



                tracker = SelectionTracker.Builder<Long>(
                    "mySelection",
                    fragmentMainRecyclerNote,
                    StableIdKeyProvider(fragmentMainRecyclerNote),
                    MyItemDetailLookup(fragmentMainRecyclerNote),
                    StorageStrategy.createLongStorage()
                ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).also {

                }.build()

                NoteAdapter.tracker = tracker
            }


        }
        return binding.root

    }
    /**
     * [setSearchCardClickListener] used to apply transition and click listener to [FragmentMainBinding.fragmentMainSearchCard]
     */
    private fun FragmentMainBinding.setSearchCardClickListener(){
        trans()
       fragmentMainSearchCard.setOnClickListener { view ->
           val name = "fragmentSearchRootTransition"
           val extras  = FragmentNavigatorExtras(view to name)
           findNavController().navigate(MainFragmentDirections.actionMainFragmentToSearchFragment(), extras)

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

                            NoteAdapter.addList(datas.filter {
                                it.deleted == false
                            })
                        }


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
    private suspend fun FragmentMainBinding.setUpCollections(){
        collectionsAdapter = NoteCollectionsAdapter()
        fragmentMainRecyclerCollection.adapter = collectionsAdapter
        observeNoteCollection()
        setCollectionAddListener()
    }
    private suspend fun observeNoteCollection(){
        dataProvider.getAllCollections().observe(viewLifecycleOwner){
            lifecycleScope.launch(Main){
                when(it){
                    is Result.FAILED -> {

                    }
                    is Result.LOADING -> {

                    }
                    is Result.NULL_VALUE -> {

                    }
                    is Result.SUCCESS<*> -> {
                        it.data as Flow<List<NoteCollections>>
                        it.data.collect { datas  ->

                            collectionsAdapter.addList(datas.filterNot {it.deleted})
                        }
                    }
                }
            }
        }
    }
    private fun FragmentMainBinding.setCollectionAddListener(){
        fragmentMainAddCollection.setOnClickListener {
           requireContext().dialog({
               requestWindowFeature(Window.FEATURE_NO_TITLE)
           },R.layout.fragment_main_add_collection_dialog) {

               window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                   this@dialog.findViewById<EditText>(R.id.fragment_main_add_collection_dialog_collection_name).doOnTextChanged { text, start, before, count ->
                       if(!text.isNullOrEmpty()) {
                           this@dialog.findViewById<TextView>(R.id.fragment_main_add_collection_dialog_add_button).setOnClickListener {
                           lifecycleScope.launch {
                               dataProvider.addCollection(
                                   NoteCollections(
                                       0,
                                       text.toString(),
                                       null,
                                       false
                                   )
                               ).observe(viewLifecycleOwner){
                                   when(it){
                                       is Result.FAILED -> {}
                                       is Result.LOADING -> {}
                                       is Result.NULL_VALUE -> {}
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
    /***/
    override fun onDetach() {
        super.onDetach()
        requireActivity().window.navigationBarColor = requireContext().resolver(R.attr.colorSurface)
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

    override fun onPause() {
        super.onPause()
        Log.i("$TAG@76", "Paused")
    }
    private fun trans(){
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