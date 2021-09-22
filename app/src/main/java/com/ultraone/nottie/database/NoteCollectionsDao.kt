package com.ultraone.nottie.database

import androidx.room.*
import com.ultraone.nottie.model.NoteCollections
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteCollectionsDao {
    @Query("SELECT * FROM note_collections_")
    fun getAllCollections(): Flow<List<NoteCollections>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCollections(collections: NoteCollections)

    @Update
    suspend fun updateCollections(collections: NoteCollections)
}