package com.ultraone.nottie.fragment

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.ultraone.nottie.R
import com.ultraone.nottie.databinding.FragmentNoteTakingSheetBinding


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

    private var _binding: FragmentNoteTakingSheetBinding? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent.parent.parent as View).fitsSystemWindows = true
    }
    private lateinit var resultLauncher: ActivityResultLauncher<String>
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {}



    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNoteTakingSheetBinding.inflate(inflater, container, false)

        binding.fragmentNoteTakingSheetInsertImage.setOnClickListener {
            resultLauncher.launch("image/*")
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