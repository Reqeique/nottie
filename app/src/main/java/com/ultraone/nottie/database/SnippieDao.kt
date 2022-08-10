package com.ultraone.nottie.database

import androidx.room.*
import com.ultraone.nottie.model.Snippie
import kotlinx.coroutines.flow.Flow
@Dao
interface SnippieDao {

    @Query("SELECT * FROM snippie_ ORDER BY ROWID ASC LIMIT 1")
     fun getSnippie(): Flow<Snippie>

    @Update
    suspend fun updateSnippie(snippie: Snippie): Int

    @Insert(onConflict= OnConflictStrategy.REPLACE)
    suspend fun addSnippie(snippie: Snippie): Long


}