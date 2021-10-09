package com.ultraone.nottie

import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils.split
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteAttachmentAndOther
import com.ultraone.nottie.util.existIn
import com.ultraone.nottie.util.invoke
import com.ultraone.nottie.util.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import java.util.regex.Pattern
import kotlin.time.seconds

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun `test copy function`(){
        val note = Note(0, null, "Nuke" ,null,null, false)
        update(note)
    }
    @Test
    @ExperimentalStdlibApi
    fun `test list update function`(){
        val list = listOf("hello","bruh", "hh")
        println(list.update("hhow"))
    }
    fun update(note: Note){
        val mNote = Note(0, "Bruh", null ,null,null, false)
        val copied = mNote.copy(title = note.title, mainNote = note.mainNote, dateTime = note.mainNote, attachmentAndOthers = note.attachmentAndOthers, deleted =  note.deleted)
        println(copied)
    }
    @Test
    fun `print true or false`(){
        print(12 + 8)
    }
    fun test(x: Int, y: Int){
        if(x > y){
             println("$y and ($x - ${x - y})")
        }
    }

}

