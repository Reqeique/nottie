package com.ultraone.nottie.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "note_collections_")
data class NoteCollections @JvmOverloads constructor (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var collectionName: String,
    var deleted: Boolean,
    var isVisible: Boolean,
    var dateTime: String
): Parcelable