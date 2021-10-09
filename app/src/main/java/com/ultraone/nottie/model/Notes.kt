package com.ultraone.nottie.model

import android.os.Parcelable
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteAttachmentAndOther(
    val archived: Boolean?,
    val collectionId: Int?,
    val pinned: Boolean?,
    val fileUri: List<String?>,

    val color: String?,
): Parcelable

@Parcelize
@Entity(tableName = "note_")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title : String?,
    val mainNote : String?,
    val dateTime : String?,
    @Embedded val attachmentAndOthers: NoteAttachmentAndOther?,
    val deleted: Boolean?
): Parcelable

object NotesData {
    val all_notes_name = stringPreferencesKey("all notes")
    val all_notes = stringSetPreferencesKey("notes")
    val id = stringPreferencesKey("id")
    val title = stringPreferencesKey("title")
    val note = stringPreferencesKey("note")
    val deleted = booleanPreferencesKey("deleted")
    val image_uri = stringPreferencesKey("image uri")
    val date_time = stringPreferencesKey("date time")
}
