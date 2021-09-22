package com.ultraone.nottie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ultraone.nottie.databinding.FragmentMainAccountBinding
import com.ultraone.nottie.util.invoke

class MainAccountFragment : Fragment() {
    private lateinit var binding: FragmentMainAccountBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainAccountBinding.inflate(inflater, container, false)
        binding
        {

        }
        return binding.root
    }
}