package com.ultraone.nottie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ultraone.nottie.coverters.Converters
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections


@Database(entities = [Note::class, NoteCollections::class], version =1, exportSchema = false)
@TypeConverters(value = [Converters::class])
abstract class MainDatabase: RoomDatabase() {
    abstract fun notesDao() : NotesDao
    abstract fun collectionDao(): NoteCollectionsDao
    companion object {
        @Volatile
        private var INSTANCE: MainDatabase?  = null

        fun getDatabase(`this`: Context): MainDatabase {
            val temp = INSTANCE
            if (temp != null) {
                return temp
            }
            synchronized(this){

                val instance = Room.databaseBuilder(`this`, MainDatabase::class.java, "main_database").build()
                INSTANCE = instance
                return instance
            }


        }
    }
}