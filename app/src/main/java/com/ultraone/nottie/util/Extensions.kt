package com.ultraone.nottie.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.LayoutRes
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.ultraone.nottie.R
import com.ultraone.nottie.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.*

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
 *    //todo
 * }
 * context{
 *    //todo
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



infix fun Note.existIn(notes: List<Note>): Boolean = notes.map(Note::id).contains(this.id)
