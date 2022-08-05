package com.ultraone.nottie.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ultraone.nottie.util.Request
import kotlinx.coroutines.flow.MutableStateFlow

class NoteTakingFragmentViewModel: ViewModel() {
    val noteId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val collectionId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val collectionId2: MutableStateFlow<Int?> = MutableStateFlow(null)
    val uriListener: MutableStateFlow<Request?> = MutableStateFlow(null)
}