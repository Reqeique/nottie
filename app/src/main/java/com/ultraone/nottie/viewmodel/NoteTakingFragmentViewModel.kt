package com.ultraone.nottie.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class NoteTakingFragmentViewModel: ViewModel() {
    val noteId: MutableLiveData<Int?> = MutableLiveData(null)
}