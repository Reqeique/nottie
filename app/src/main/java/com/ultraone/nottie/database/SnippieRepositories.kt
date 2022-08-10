package com.ultraone.nottie.database

import com.ultraone.nottie.model.Snippie
import kotlinx.coroutines.flow.Flow

class SnippieRepositories(private val snippieDao: SnippieDao) {
    suspend fun updateSnippie(snippie: Snippie): Int = snippieDao.updateSnippie(snippie)
    suspend fun addSnippie(snippie: Snippie): Long = snippieDao.addSnippie(snippie)
    fun getSnippie(): Flow<Snippie> = snippieDao.getSnippie()
}