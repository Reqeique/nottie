package com.ultraone.nottie.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.flow.MutableStateFlow

class NoteTakingFragmentViewModel: ViewModel() {
    val noteId: MutableStateFlow<Int?> = MutableStateFlow(null)
}