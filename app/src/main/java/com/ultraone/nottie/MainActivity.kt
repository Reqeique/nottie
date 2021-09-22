package com.ultraone.nottie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler

import android.os.Looper
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.ultraone.nottie.util.invoke


import com.ultraone.nottie.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity(){
    fun main(arr: Array<String>){
        //hello
        print("how to makr")
        println("heck is goung on")
    }
    lateinit var controller: NavController
    lateinit var config: AppBarConfiguration
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding {
            this@MainActivity {

            }
        }
    //    val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

    }
}//secret