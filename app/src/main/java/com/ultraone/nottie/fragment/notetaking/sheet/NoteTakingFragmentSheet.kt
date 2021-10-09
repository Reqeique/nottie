package com.ultraone.nottie.fragment.notetaking.sheet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ultraone.nottie.R
import com.ultraone.nottie.databinding.FragmentNoteTakingSheetBinding
import com.ultraone.nottie.util.IMAGE_REQUEST_CODE
import com.ultraone.nottie.util.Request
import com.ultraone.nottie.util.fileType
import com.ultraone.nottie.viewmodel.NoteTakingFragmentViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


// TODO: Customize parameter argument names

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    NoteTakingFragmentSheet.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class NoteTakingFragmentSheet : BottomSheetDialogFragment() {
    private val noteTakingFragmentViewModel: NoteTakingFragmentViewModel by activityViewModels()
    companion object {
        const val TAG = "::NoteTakingFragmentS"
    }
    private var _binding: FragmentNoteTakingSheetBinding? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent.parent.parent as View).fitsSystemWindows = true
    }
    private lateinit var resultLauncher: ActivityResultLauncher<Array<String>>
    private val binding get() = _binding!!
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {

            lifecycleScope.launch {
                if(it == null) return@launch
              Log.d("$TAG@63", "${it.toString().fileType(requireContext())}")
               noteTakingFragmentViewModel.uriListener.emit(Request(it))
               val takeFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION
               try {
                   activity?.contentResolver?.takePersistableUriPermission(it,takeFlag)
               } catch(e: SecurityException){

               }

               dismiss()
           }
       }




    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNoteTakingSheetBinding.inflate(inflater, container, false)

        binding.fragmentNoteTakingSheetInsertImage.setOnClickListener {
            resultLauncher.launch(arrayOf("image/*"))
        }
        binding.fragmentNoteTakingSheetInsertDocument.setOnClickListener {
            resultLauncher.launch(arrayOf("application/*", "text/*"))
        }
        binding.fragmentNoteTakingSheetInsertVideo.setOnClickListener {
            resultLauncher.launch(arrayOf("video/*"))
        }
        binding.fragmentNoteTakingSheetInsertAudio.setOnClickListener {
            resultLauncher.launch(arrayOf("audio/*"))
        }
//        binding.fragmentNoteTakingSheetInsertFile.setOnClickListener {
//            resultLauncher.launch("documents/*")
//        }


        lifecycleScope.launch {
            noteTakingFragmentViewModel.uriListener.collect {
                Log.d("HH","request = $it")
            }
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}