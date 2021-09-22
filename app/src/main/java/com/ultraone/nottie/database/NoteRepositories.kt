package com.ultraone.nottie.database

import androidx.lifecycle.LiveData
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.Notes
import kotlinx.coroutines.flow.Flow

open class NoteRepositories(private val notesDao: NotesDao) {
    suspend fun addNote(note: Note): Long = notesDao.addNote(note)
    val getAllNotes : Flow<List<Note>> = notesDao.getAllNotes()
    suspend fun deleteNote(note: Note): Unit = notesDao.deleteNote(note)
    suspend fun updateNote(note: Note): Unit = notesDao.updateNote(note)
    suspend fun getAllNotesOrderedById(): Notes = Notes(notesDao.getAllNotesOrderedById())
}