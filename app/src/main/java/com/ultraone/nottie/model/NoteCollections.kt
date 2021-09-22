package com.ultraone.nottie.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "note_collections_")
data class NoteCollections @JvmOverloads constructor (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var collectionName: String,
    val notesId: List<Int>?,
    var deleted: Boolean
): Parcelable