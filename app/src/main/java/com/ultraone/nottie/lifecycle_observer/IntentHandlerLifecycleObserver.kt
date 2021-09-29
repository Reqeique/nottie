package com.ultraone.nottie.lifecycle_observer

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

//class IntentHandlerLifecycleObserver(private var registry: ActivityResultRegistry,key): DefaultLifecycleObserver {
//    lateinit var getContent: ActivityResultLauncher<String>
//    override fun onCreate(owner: LifecycleOwner) {
//        getContent = registry.register()
//    }
//}