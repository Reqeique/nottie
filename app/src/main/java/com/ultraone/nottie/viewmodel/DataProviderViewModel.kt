package com.ultraone.nottie.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.ultraone.nottie.database.MainDatabase
import com.ultraone.nottie.database.NoteCollectionsRepositories
import com.ultraone.nottie.database.NoteRepositories

import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteAttachmentAndOther
import com.ultraone.nottie.model.NoteCollections
import com.ultraone.nottie.model.Result
import com.ultraone.nottie.util.existIn
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.concurrent.thread

@SuppressLint("StaticFieldLeak")
class DataProviderViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "::DataProviderVM"
    }

    private val noteRepo: NoteRepositories
    private val noteCollectionsRepo: NoteCollectionsRepositories
    private val context = getApplication<Application>().applicationContext

    init {

        val noteDao = MainDatabase.getDatabase(application).notesDao()
        noteRepo = NoteRepositories(noteDao)
        noteCollectionsRepo = NoteCollectionsRepositories(MainDatabase.getDatabase(application).collectionDao())
        viewModelScope.launch(Main) {
            getAllNotes()

        }


    }

//    inline fun heck(crossinline trans: suspend (n: Note) -> Unit, noinline heck: ( ) -> Unit){
//        thread(block = trans)
//
//    }

    suspend fun updateOrCreateNewNote(note: Note) = liveData {
        Log.d("$TAG@51", "Calling update or create new note")
        coroutineScope {
            viewModelScope.launch {

                noteRepo.getAllNotes.collect { notes ->
                    if (note existIn notes) {
                        updateNote(note).asFlow().collect {
                            when (it) {
                                is Result.FAILED -> {

                                }
                                is Result.LOADING -> {

                                }
                                is Result.NULL_VALUE -> {

                                }
                                is Result.SUCCESS<*> -> {
                                    emit(Result.SUCCESS(it.data.toString()))

                                }
                            }

                        }

                    } else if (!(note existIn notes)) {
                        addNote(note).asFlow().collect {
                            when (it) {
                                is Result.FAILED -> {

                                }
                                is Result.LOADING -> {

                                }
                                is Result.NULL_VALUE -> {

                                }
                                is Result.SUCCESS<*> -> {
                                    emit(Result.SUCCESS(it.data.toString()))

                                }
                            }
                        }
                    }
                }
            }.join()
        }
    }
    /**
     * those extends from [NoteRepositories] and used to weather update, pin, add , delete specific note on the database*/
    suspend fun addNote(note: Note): LiveData<Result> = liveData {
        coroutineScope {
             //if(note.isNotNull()) {
            val mainJob = viewModelScope.async {
                emit(Result.LOADING)
                noteRepo.addNote(note)
             //   emit(Result.SUCCESS(2L))
                // emit(Result.SUCCESS(null))
            }
            mainJob.join()
            mainJob.invokeOnCompletion {
                Log.d(TAG, "Saved")
                launch {
                    Log.d(TAG, "Saved [Coroutine] + $it")
                    if (it != null) emit(Result.FAILED(it)) else emit(Result.SUCCESS(mainJob.await()))
                }
            }
        }
        // }
    }
    val getAllNote : LiveData<Flow<List<Note>>> = liveData {
        emit(noteRepo.getAllNotes)
    }
    suspend fun updateNote(note: Note): LiveData<Result> = liveData {
        coroutineScope {
            val mainJob = viewModelScope.async {
                emit(Result.LOADING)
                noteRepo.updateNote(note)
            }
            mainJob.join()
            mainJob.invokeOnCompletion {
                Log.d(TAG, "Saved")
                launch {
                    if (it != null) emit(Result.FAILED(it)) else emit(Result.SUCCESS(mainJob.await()))
                }
            }
        }
    }



    suspend fun deleteNote(note: Note) = liveData<Result> {
        coroutineScope {
            val mainJob = viewModelScope.launch {
                emit(Result.LOADING)
                noteRepo.updateNote(
                    Note(
                        note.id,
                        note.title,
                        note.mainNote,
                        note.dateTime,
                        note.attachmentAndOthers,
                        true
                    )
                )

            }
            mainJob.join()
            mainJob.invokeOnCompletion {
                Log.d(TAG, "DELETED")
                launch {
                    if (it != null) emit(Result.FAILED(it)) else emit(Result.SUCCESS(null))
                }
            }

        }
    }


    suspend fun getAllNotes() = liveData<Result> {


        emit(Result.SUCCESS(noteRepo.getAllNotes))


    }
    /**
     * */
    /** these set of function is used to perform [Update], /. in [NoteCollectionsDao]]
     * */
    suspend fun addCollection(noteCollections: NoteCollections):LiveData<Result> = liveData {
        coroutineScope {
            val mainJob = viewModelScope.launch {
                emit(Result.LOADING)
                noteCollectionsRepo.addCollections(noteCollections)
            }
            mainJob.join()
            mainJob.invokeOnCompletion {
                launch {
                    if (it != null) emit(Result.FAILED(it)) else emit(Result.SUCCESS(null))
                }
            }
        }
    }
    suspend fun getAllCollections(): LiveData<Result> = liveData {
        coroutineScope {
            val mainJob = viewModelScope.launch {
                emit(Result.LOADING)
                emit(Result.SUCCESS(noteCollectionsRepo.getAllCollections))
            }
            mainJob.join()
        }
    }
    suspend fun updateCollections(noteCollections: NoteCollections) = liveData {
        coroutineScope {
            val mainJob = viewModelScope.launch {
                emit(Result.LOADING)
                noteCollectionsRepo.updateCollections(noteCollections)
            }
            mainJob.join()
            mainJob.invokeOnCompletion {
                launch {
                    if (it != null) emit(Result.FAILED(it)) else emit(Result.SUCCESS(null))
                }
            }

        }
    }
    suspend fun deleteCollections(noteCollections: NoteCollections) = liveData {
        coroutineScope {
            val mainJob = viewModelScope.launch {
                emit(Result.LOADING)
                val deleted = noteCollections.copy(deleted =  true)
                noteCollectionsRepo.updateCollections(deleted)
            }
            mainJob.join()
            mainJob.invokeOnCompletion {
                launch {
                    if (it != null) emit(Result.FAILED(it)) else emit(Result.SUCCESS(null))
                }
            }

        }
    }

}
