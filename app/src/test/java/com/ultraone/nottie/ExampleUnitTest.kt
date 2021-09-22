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
    private suspend fun updateOrCreateNewNote(myNote:suspend (Note) -> Note) {
        val note: Note
        var attachmentAndOther = NoteAttachmentAndOther(
            null,
            null,
            null,
            null,
            null
        )
        var mNote = Note(
            0,
            null,
            null,
           null,
            attachmentAndOther,
            false
        )

        note = myNote(mNote)
        note(::print)


    }
    @Test
    fun `test existIn`(){
        val note1 = Note(1,"f","h","",null, false)
        val note2 = note1.copy(title = "h"
        )
        val note3 = note1.copy(0)
        val list = listOf(note1, note2)
        println(note3 existIn list)
    }

    @Test
    fun `test the type converter`(){
        fun intToList(value : Int): List<Int>?{
            val type = object : TypeToken<List<Int>?>() {}.type
            return Gson().fromJson(value.toString(), type)
        }
        intToList(0)(::println)
    }

    fun `test lambda`() = runBlocking{
        updateOrCreateNewNote {
            var note = it
            note = note.copy(title = "title")
            note.copy(dateTime = "time goes brr")
            note.copy(attachmentAndOthers =  note.attachmentAndOthers?.copy(pinned = true))
            note
        }
    }

    @ExperimentalStdlibApi

    fun `check the filter`(){
        val month by lazy {
            (1..31).toMutableList()
        }
        val currentDate = month.random()

        month.mapIndexed{ i, it ->
            when {
                i == currentDate.minus(-1) -> {
                    100
                }
                i != currentDate.minus(-1) -> {
                    it
                }


                else -> 1000
            }
        }(::println)
//        month{
//            var x  = filter {
//                it == currentDate
//            }.map {
//                it*10
//            }
//
//            var y = filterNot {
//                it == currentDate
//            }
//            buildList {
//                addAll(x)
//                addAll(y)
//
//            }.sortedBy { it }(::print)
//
//        }
    }


   // @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        println("""THIS IS A TEST
        |=================|
        |
    """.trimMargin())
       // var formattedText = "This is a sample"





        fun formatter(textToBeFormatted: String, position: List<Pair<Pair<Int, Int>, Int>>): String{
            var formattedText: String = textToBeFormatted
            val builderN = StringBuilder(formattedText)
            position.forEach {
//                    Log.d("LOG-G", "$it")
                when (it.second) {
                    1 -> {
                        formattedText = builderN.insert(it.first.first, "**").insert(it.first.second+ 2 , "**").toString()
                    }
//                        2 -> {
//                            formattedText = builder.insert(it.first.first, "*").insert(it.first.second, "*").toString()
//                        }
                }
            }
            return formattedText
        }
        formatter("This is the longest text i have wrote",(listOf((1 to 2) to 1 ,(9 to 14) to 1)
                )).let {println(it)}

        var test = "This is a test"
        val builder = StringBuilder(test)
        listOf((1 to 2), (4 to 7)).forEach {
            test = builder.insert(it.first, "[").insert(it.second, "]").toString()
        }
        println(test)
        listOf((1 to 2), (4 to 7)).fold(listOf<String>(),{f, fj -> listOf()})

        listOf<Int>().sum()

            val thisIsATest = "** bold** normal **even bold** normal2"
        "\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)".toRegex().replace(thisIsATest, "\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)").let{
            println(it)
        }
    }
    fun interface MCall{

        fun updateMyText(bruh: String)

    }


    @Test
    fun check_the_call_back(){
//        val callBack : MCall
//        var st: String? = null
//        callBack = MCall {
//            st = it
//        }
//        callBack.updateMyText("bruh")
//        print(st)
        val b = Testing()
                b.updateMyText("hmm")
        b.revealTheText()
        print(b.`return`())

    }

}

fun interface MCall2{
    fun updateMyText(text: String)
}
class Testing : MCall2 {// assume this is activity
    var st: String? = null//assume this is text view
    override fun updateMyText(text: String){
        st = text

    }
    fun revealTheText(){
        print(st)
    }
    fun `return`(): String? = st


}
//fun main(){// assume this is where you apply change
//    Testing().updateMyText("hmm")
//}