package com.ultraone.nottie.database

import com.ultraone.nottie.model.NoteCollections
import kotlinx.coroutines.flow.Flow

class NoteCollectionsRepositories(private val noteCollectionsDao: NoteCollectionsDao) {
    val getAllCollections: Flow<List<NoteCollections>> = noteCollectionsDao.getAllCollections()
    suspend fun addCollections(noteCollections: NoteCollections): Unit = noteCollectionsDao.addCollections(noteCollections)
    suspend fun updateCollections(noteCollections: NoteCollections): Unit = noteCollectionsDao.updateCollections(noteCollections)
}