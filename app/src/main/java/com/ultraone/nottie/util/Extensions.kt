package com.ultraone.nottie.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Editable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.LayoutRes
import androidx.core.graphics.drawable.toIcon
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import com.ultraone.nottie.R
import com.ultraone.nottie.model.Note
import com.ultraone.nottie.model.NoteCollections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContentResolver
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.load
import coil.request.ImageRequest


/**
 * Alternatives for [DataStore] preference
 */
fun mainTest() {
    generateSequence(::readLine) {
        ""
    }
}

fun safeDataStorePreference(
    name: String,
    thisRef: Context,
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    produceMigrations: (Context) -> List<DataMigration<Preferences>> = { listOf() },
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = corruptionHandler,
        migrations = produceMigrations(thisRef.applicationContext),
        scope = scope
    ) {
        thisRef.preferencesDataStoreFile(name)
    }
}

/**
 * Invokes [Any] type as a block
 * usage
 * ```kt
 * context.invoke {
 *
 * }
 * context{
 *
 * }
 * ```
 */
public inline operator fun <R, T> T.invoke(block: T.() -> R): R {

    block()
    return block(this)
}


/**
 * Helper extensions for [SharedPreferences]
 */
fun SharedPreferences.Editor.putString(
    key: Preferences.Key<String>,
    value: String
): SharedPreferences.Editor = putString(key.name, value)

fun SharedPreferences.Editor.putBoolean(
    key: Preferences.Key<Boolean>,
    value: Boolean
): SharedPreferences.Editor = putBoolean(key.name, value)

fun SharedPreferences.getString(key: Preferences.Key<String>, value: String?): String? =
    getString(key.name, value)

fun SharedPreferences.getBoolean(key: Preferences.Key<Boolean>, value: Boolean): Boolean =
    getBoolean(key.name, value)

/**
 *
 */
fun Note.isNotNull(): Boolean =
    (title != null && dateTime != null && mainNote != null && deleted != null)


fun Context.toast(message: CharSequence, longToast: Boolean = false) {
    if (longToast) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    } else if (!longToast) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

fun Fragment.toast(message: CharSequence, longToast: Boolean = false) {
    (requireActivity()){
        toast(message, longToast)
    }
}


val currentTime: String
    @SuppressLint("SimpleDateFormat")
    get() {
        return SimpleDateFormat("yyMMddhhmmss.SSS").format(Date())
    }

/**
 * [decodeToTimeAndDate] function used to fetch from year to sec in the form of [hh:min day/month/year] from [currentTime]
 * */
fun String.decodeToTimeAndDate(): String {

    val (year, month, day, hour, minute, sec) = chunked(2)
    return "$hour:$minute  $day/$month/$year"

}

/**
 * [decodeDate] function used to fetch the date from [currentTime] variable as [Int]*/
fun String.decodeDate(): Int {
    val (_,_, day, _, _, _) = chunked(2)
    return day.toInt()

}

private operator fun <E> List<E>.component6(): E = this[5]
fun CharSequence.toEditable(): Editable = Editable.Factory().newEditable(this)


val Fragment.superFragmentManager
    get() = requireActivity().supportFragmentManager


fun View.longSnackBar(message: CharSequence): Unit =
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()

fun View.shortSnackBar(message: CharSequence): Unit =
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()

/**
 * [resolver] function is used to fetch the color from attribute
 * eg
 * ```kt
 * context.resolver(R.attr.attrname)
 * ``` *
 * */
fun Context.resolver(
    @AttrRes attrRes: Int,
    typedValue: TypedValue = TypedValue()

): Int {


    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}

/**
 * a null-like variable for navigation component*/
const val NULL_VALUE_INT = -2

/**
 * [invokeSelectableState] is function which return the view and apply re-selectable state which invoke [View.isSelected]
 * when the button is clicked by the factor which is divisible by 2 it change [View.isSelected] value to {false} and vice-versa
 * */
fun <T : View> View.invokeSelectableState(state: (Boolean?) -> Unit): View {
    var clicks = 0
    setOnClickListener {
        clicks++

        when {
            clicks % 2 == 0 -> {
                if(isSelected) {
                    isSelected = false
                    state(false)
                }else if(!isSelected){
                    isSelected = true
                    state(true)
                }
            }
            clicks % 2 != 0 -> {
                if(isSelected) {
                    isSelected = false
                    state(false)
                }else if(!isSelected){
                    isSelected = true
                    state(true)
                }
            }
        }


    }

    return this as T

    //  setOnClickListener(onClick)



}

fun Context.dialog(
    feature: Dialog.() -> Unit,
    @LayoutRes contentView: Int,
    mDialog: Dialog.() -> Unit

){
    Dialog(this).apply(feature).apply{ setContentView(contentView) }.also (mDialog)
}

fun Context.dialog(
    feature: Dialog.() -> Unit,
    contentView: View,
    mDialog: Dialog.() -> Unit
){
    Dialog(this).apply(feature).apply{ setContentView(contentView)}.also(mDialog)
}

infix fun Note.existIn(notes: List<Note>): Boolean = notes.map(Note::id).contains(this.id)


fun LiveData<*>.observe(lifecycleOwner: LifecycleOwner){
    this.observe(lifecycleOwner, {})
}

fun List<NoteCollections>.filterById(id: Int): NoteCollections = first { it.id == id }
fun List<NoteCollections>.filterByIdOrNull(id: Int?): NoteCollections? = firstOrNull { it.id == id }
fun List<Note>.filterById(id: Int): Note = first { it.id == id}
fun List<Note>.filterByIdOrNull(id: Int?) = firstOrNull { it.id == id }
operator fun List<Note>.get(id: Int) = filterById(id)
operator fun List<NoteCollections>.get(id: Int) = filterById(id)

@ExperimentalStdlibApi
fun <T> List<T>.update(element: T): List<T>{
    val emptyList = mutableListOf<T>()
    emptyList.addAll(this)
    emptyList.add(element)

    return emptyList
}

sealed interface FileType
class IMAGE(val extension: String) : FileType
class VIDEO(val extension: String): FileType
class AUDIO(val extension: String): FileType
class Other(val extension: String): FileType
class DOCUMENT(val extension: String): FileType

/**
 * extension function [fileType] is used to determine [Uri]'s content type based on sealed interface [FileType]
 * */
fun String.fileType(context: Context): FileType {
    val file = context.contentResolver.getType(this.toUri())
    val (type: String?, extension: String?) = file?.split("/")?.first() to (file?.split("/")?.get(1) ?: "")

    return when(type){
        "audio" -> AUDIO(extension)
        "video" -> VIDEO(extension)
        "application"  -> DOCUMENT(extension)
        "text" -> DOCUMENT(extension)
        "image" -> IMAGE(extension)

        else -> Other("$type/$extension")
    }
}


inline fun ImageView.loadPreview(
    uri: Uri?,
    isOpened : Boolean = false,
    builder: ImageRequest.Builder.() -> Unit = {},

) {
    load(uri) {
        if (isOpened) size(860, 860) else size(240, 240)
        builder()
    }
}
inline fun ImageView.loadPreview(
    uri: String?,
    isOpened : Boolean = false,
    builder: ImageRequest.Builder.() -> Unit = {}

) {
    load(uri) {
        if (isOpened) size(860, 860) else size(240, 240)
        builder()
    }
}


fun getAlbumArtBitmap(context: Context, uri: Uri): Bitmap? {
    val retriever = MediaMetadataRetriever()
    val result = runCatching {
        retriever
            .run {
                setDataSource(context, uri)
                embeddedPicture
            }
            ?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
    }.getOrNull()

    retriever.release()

    return result
}

fun Context.getDrawableCompat(@DrawableRes resId: Int) = ContextCompat.getDrawable(this, resId)


fun View.setVisible() {
    this.visibility = View.VISIBLE
}
fun View.setGone(){
    visibility = View.GONE
}
fun View.setInvisible(){
    visibility = View.INVISIBLE
}
