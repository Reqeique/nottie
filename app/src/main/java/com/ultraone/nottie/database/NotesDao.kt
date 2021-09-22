package com.ultraone.nottie.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ultraone.nottie.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM note_")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note_ ORDER by id")
    fun getAllNotesOrderedById(): Flow<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)
//    suspend fun getNote(index: Int): Note
}