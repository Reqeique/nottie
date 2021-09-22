//
//
//
//
//import android.content.Intent
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.RectF
//import android.os.Bundle
//import android.text.InputType
//import android.text.TextWatcher
//import android.view.*
//import android.view.inputmethod.EditorInfo
//import android.widget.EditText
//import android.widget.TextView
//import androidx.activity.addCallback
//import androidx.annotation.ColorInt
//import androidx.appcompat.widget.Toolbar
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.graphics.drawable.toBitmap
//import androidx.core.view.*
//import androidx.core.widget.doOnTextChanged
//import androidx.fragment.app.clearFragmentResult
//import androidx.fragment.app.setFragmentResultListener
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.ItemTouchHelper.*
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.snackbar.BaseTransientBottomBar
//import com.google.android.material.snackbar.Snackbar
//import com.google.android.material.transition.MaterialContainerTransform
//import com.google.android.material.transition.MaterialSharedAxis
//import com.ultraone.nottie.util.views.MEditText
//import dagger.hilt.android.AndroidEntryPoint
//import io.noties.markwon.Markwon
//import io.noties.markwon.editor.MarkwonEditor
//import io.noties.markwon.editor.MarkwonEditorTextWatcher
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import org.commonmark.node.Code
//import org.qosp.notes.R
//import org.qosp.notes.data.model.Attachment
//import org.qosp.notes.data.model.Note
//import org.qosp.notes.data.model.NoteColor
//import org.qosp.notes.data.model.NoteTask
//import org.qosp.notes.databinding.FragmentEditorBinding
//import org.qosp.notes.databinding.LayoutAttachmentBinding
//import org.qosp.notes.ui.attachments.dialog.EditAttachmentDialog
//import org.qosp.notes.ui.attachments.fromUri
//import org.qosp.notes.ui.attachments.recycler.AttachmentRecyclerListener
//import org.qosp.notes.ui.attachments.recycler.AttachmentsAdapter
//import org.qosp.notes.ui.attachments.recycler.AttachmentsGridManager
//import org.qosp.notes.ui.attachments.uri
//import org.qosp.notes.ui.common.BaseDialog
//import org.qosp.notes.ui.common.BaseFragment
//import org.qosp.notes.ui.common.showMoveToNotebookDialog
//import org.qosp.notes.ui.editor.dialog.InsertHyperlinkDialog
//import org.qosp.notes.ui.editor.dialog.InsertImageDialog
//import org.qosp.notes.ui.editor.dialog.InsertTableDialog
//import org.qosp.notes.ui.editor.markdown.MarkdownSpan
//import org.qosp.notes.ui.editor.markdown.addListItemListener
//import org.qosp.notes.ui.editor.markdown.applyTo
//import org.qosp.notes.ui.editor.markdown.insertMarkdown
//import org.qosp.notes.ui.editor.markdown.setMarkdownTextSilently
//import org.qosp.notes.ui.editor.markdown.toggleCheckmarkCurrentLine
//import org.qosp.notes.ui.media.MediaActivity
//import org.qosp.notes.ui.recorder.RECORDED_ATTACHMENT
//import org.qosp.notes.ui.recorder.RECORD_CODE
//import org.qosp.notes.ui.recorder.RecordAudioDialog
//import org.qosp.notes.ui.reminders.EditReminderDialog
//import org.qosp.notes.ui.tasks.TaskRecyclerListener
//import org.qosp.notes.ui.tasks.TaskViewHolder
//import org.qosp.notes.ui.tasks.TasksAdapter
//import org.qosp.notes.ui.utils.*
//import org.qosp.notes.ui.utils.views.BottomSheet
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.util.concurrent.Executors
//import javax.inject.Inject
//
//private typealias Data = EditorViewModel.Data
//class Binder {
//    lateinit var editTextTitle : MEditText
//    lateinit var editTextCotent: MEditText
//}
//@AndroidEntryPoint
//class EditorFragment : BaseFragment(R.layout.fragment_editor) {
//    private val binding = Binder()
//    private val model: EditorViewModel by viewModels()
//
//    private val args: EditorFragmentArgs by navArgs()
//    private var snackbar: Snackbar? = null
//    private var mainMenu: Menu? = null
//    private var contentHasFocus: Boolean = false
//    private var isNoteDeleted: Boolean = false
//    private var markwonTextWatcher: TextWatcher? = null
//    private var isMarkwonAttachedToEditText: Boolean = false
//    private var onBackPressHandled: Boolean = false
//
//    @ColorInt
//    private var backgroundColor: Int = Color.TRANSPARENT
//    private var data = Data()
//
//    private var nextTaskId: Long = 0L
//    private var isList: Boolean = false
//    private var isFirstLoad: Boolean = true
//    private var formatter: DateTimeFormatter? = null
//
//    private lateinit var attachmentsAdapter: AttachmentsAdapter
//    private lateinit var tasksAdapter: TasksAdapter
//
//    @Inject
//    lateinit var markwon: Markwon
//
//    @Inject
//    lateinit var markwonEditor: MarkwonEditor
//
//    override val hasDefaultAnimation = false
//    override val toolbar: Toolbar
//        get() = binding.toolbar
//
//    private fun setupEditTexts() = with(binding) {
//        editTextTitle.apply {
//            imeOptions = EditorInfo.IME_ACTION_NEXT
//            setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
//
//            setOnEditorActionListener { v, actionId, event ->
//                when {
//                    actionId == EditorInfo.IME_ACTION_NEXT && data.note?.isList == true -> {
//                        jumpToNextTaskOrAdd(-1)
//                        true
//                    }
//                    else -> false
//                }
//            }
//
//            doOnTextChanged { text, start, before, count ->
//                // Only listen for meaningful changes
//                if (data.note == null) {
//                    return@doOnTextChanged
//                }
//
//                model.setNoteTitle(text.toString().trim())
//            }
//        }
//
//        editTextContent.apply {
//            setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
//            doOnTextChanged { text, start, before, count ->
//                // Only listen for meaningful changes, we do not care about empty text
//                if (data.note == null) {
//                    return@doOnTextChanged
//                }
//
//                model.setNoteContent(text.toString().trim())
//            }
//            setOnFocusChangeListener { v, hasFocus ->
//                contentHasFocus = hasFocus
//                setMarkdownToolbarVisibility()
//            }
//
//            setOnEditorActionListener(addListItemListener)
//        }
//
//        // Used to clear focus and hide the keyboard when touching outside of the edit texts
//        linearLayout.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) root.hideKeyboard()
//        }
//    }
//
//
//    private fun observeData() = with(binding) {
//        model.data.collect(viewLifecycleOwner) { data ->
//            if (data.note == null && data.isInitialized) {
//                return@collect run { findNavController().navigateUp() }
//            }
//
//            if (!data.isInitialized || data.note == null) return@collect
//
//            this@EditorFragment.data = data
//
//            val isConverted = data.note.isList != isList
//            val isMarkdownEnabled = data.note.isMarkdownEnabled
//            val (dateFormat, timeFormat) = data.dateTimeFormats
//
//            isList = data.note.isList
//            isNoteDeleted = data.note.isDeleted
//
//            if (isMarkdownEnabled) {
//                enableMarkdownTextWatcher()
//            } else {
//                disableMarkdownTextWatcher()
//            }
//
//            // Update Title and Content only the first the since they are EditTexts
//            if (isFirstLoad) {
//                editTextTitle.setTextSilently(data.note.title)
//                when {
//                    isList -> tasksAdapter.submitList(data.note.taskList)
//                    else -> {
//                        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
//                            editTextContent.setMarkdownTextSilently(data.note.content)
//
//                            val (selStart, selEnd) = model.selectedRange
//                            if (selStart >= 0 && selEnd <= editTextContent.length()) {
//                                editTextContent.setSelection(selStart, selEnd)
//                            }
//                        }
//                    }
//                }
//
//                nextTaskId = data.note.taskList.map { it.id }.maxOrNull()?.plus(1) ?: 0L
//            }
//
//            // We only want to update the task list when the user converts the note from text to list
//            if (isConverted) {
//                tasksAdapter.tasks.clear()
//                tasksAdapter.notifyDataSetChanged()
//                tasksAdapter.submitList(data.note.taskList)
//                editTextContent.setMarkdownTextSilently(data.note.content)
//            }
//            recyclerTasks.isVisible = isList
//
//            updateEditMode(note = data.note)
//
//            // Must be called after updateEditMode since that method changes the visibility of the inputs
//            if (isFirstLoad) requestFocusForFields()
//
//            // Also set text of preview textviews
//            textViewTitlePreview.text = data.note.title.ifEmpty { getString(R.string.indicator_untitled) }
//
//            if (isMarkdownEnabled) {
//                // Seems to be crashing often without wrapping it in a post { } call
//                textViewContentPreview.post {
//                    markwon.applyTo(textViewContentPreview, data.note.content) {
//                        tableReplacement = { Code(getString(R.string.message_cannot_preview_table)) }
//                        maximumTableColumns = 15
//                    }
//                }
//            } else {
//                textViewContentPreview.text = data.note.content
//            }
//
//            setupMenuItems(data.note, data.note.reminders.isNotEmpty())
//
//            // Update notebook indicator
//            notebookView.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                requireContext().getDrawableCompat(R.drawable.ic_notebook),
//                null,
//                requireContext().getDrawableCompat(if (data.notebook == null) R.drawable.ic_add else R.drawable.ic_swap),
//                null
//            )
//            notebookView.text = data.notebook?.name ?: getString(R.string.notebooks_unassigned)
//
//            // Update fragment background colour
//            data.note.color.resId(requireContext())?.let { resId ->
//                backgroundColor = resId
//                root.setBackgroundColor(resId)
//                containerBottomToolbar.setBackgroundColor(resId)
//                toolbar.setBackgroundColor(resId)
//            }
//
//            // Update date
//            val offset = ZoneId.systemDefault().rules.getOffset(Instant.now())
//            val creationDate = LocalDateTime.ofEpochSecond(data.note.creationDate, 0, offset)
//            val modifiedDate = LocalDateTime.ofEpochSecond(data.note.modifiedDate, 0, offset)
//
//            formatter =
//                DateTimeFormatter.ofPattern("${getString(dateFormat.patternResource)}, ${getString(timeFormat.patternResource)}")
//
//            textViewDate.isVisible = data.showDates
//            if (formatter != null && data.showDates) {
//                textViewDate.text =
//                    getString(R.string.indicator_note_date, creationDate.format(formatter), modifiedDate.format(formatter))
//            }
//
//            // We want to start the transition only when everything is loaded
//            binding.root.doOnPreDraw {
//                startPostponedEnterTransition()
//            }
//
//            if (isNoteDeleted) {
//                snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_INDEFINITE)
//                    .setText(getString(R.string.indicator_deleted_note_cannot_be_edited))
//                    .setAction(getString(R.string.action_restore)) { view ->
//                        activityModel.restoreNotes(data.note)
//                        activity?.onBackPressed()
//                    }
//                snackbar?.show()
//                snackbar?.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
//                    override fun onShown(transientBottomBar: Snackbar?) {
//                        super.onShown(transientBottomBar)
//                        scrollView.apply {
//                            setPadding(paddingLeft, paddingTop, paddingRight, snackbar?.view?.height ?: paddingBottom)
//                        }
//                    }
//                })
//            }
//
//            // Update attachments
//            attachmentsAdapter.submitList(data.note.attachments)
//
//            // Update tags
//            containerTags.removeAllViews()
//            data.note.tags.forEach { tag ->
//                containerTags.addView(
//                    TextView(ContextThemeWrapper(requireContext(), R.style.TagChip)).apply {
//                        text = "# ${tag.name}"
//                    }
//                )
//            }
//
//            isFirstLoad = false
//        }
//    }
//
//    private fun setupListeners() = with(binding) {
//        bottomToolbar.setOnMenuItemClickListener {
//
//            val span = when (it.itemId) {
//                R.id.action_insert_bold -> MarkdownSpan.BOLD
//                R.id.action_insert_italics -> MarkdownSpan.ITALICS
//                R.id.action_insert_strikethrough -> MarkdownSpan.STRIKETHROUGH
//                R.id.action_insert_code -> MarkdownSpan.CODE
//                R.id.action_insert_quote -> MarkdownSpan.QUOTE
//                R.id.action_insert_heading -> MarkdownSpan.HEADING
//                R.id.action_insert_link -> {
//                    clearFragmentResult(MARKDOWN_DIALOG_RESULT)
//                    InsertHyperlinkDialog
//                        .build(editTextContent.selectedText ?: "")
//                        .show(parentFragmentManager, null)
//                    null
//                }
//                R.id.action_insert_image -> {
//                    clearFragmentResult(MARKDOWN_DIALOG_RESULT)
//                    InsertImageDialog
//                        .build(editTextContent.selectedText ?: "")
//                        .show(parentFragmentManager, null)
//                    null
//                }
//                R.id.action_insert_table -> {
//                    clearFragmentResult(MARKDOWN_DIALOG_RESULT)
//                    InsertTableDialog().show(parentFragmentManager, null)
//                    null
//                }
//                R.id.action_toggle_check_line -> {
//                    editTextContent.toggleCheckmarkCurrentLine()
//                    null
//                }
//                R.id.action_scroll_to_top -> {
//                    scrollView.smoothScrollTo(0, 0)
//                    editTextContent.setSelection(0)
//                    null
//                }
//                R.id.action_scroll_to_bottom -> {
//                    scrollView.smoothScrollTo(0, editTextContent.bottom + editTextContent.paddingBottom + editTextContent.marginBottom)
//                    editTextContent.setSelection(editTextContent.length())
//                    null
//                }
//                else -> return@setOnMenuItemClickListener false
//            }
//            editTextContent.insertMarkdown(span ?: return@setOnMenuItemClickListener false)
//            true
//        }
//
//        notebookView.setOnClickListener {
//            data.note?.let { showMoveToNotebookDialog(it) }
//        }
//
//        actionAddTask.setOnClickListener {
//            addTask()
//        }
//    }
//
//    private fun setupMarkdown() {
//        markwonTextWatcher = MarkwonEditorTextWatcher.withPreRender(
//            markwonEditor, Executors.newCachedThreadPool(),
//            binding.editTextContent
//        )
//    }
//
//    private fun enableMarkdownTextWatcher() = with(binding) {
//        if (markwonTextWatcher != null && !isMarkwonAttachedToEditText) {
//            // TextWatcher is created and currently not attached to the EditText, we attach it
//            editTextContent.addTextChangedListener(markwonTextWatcher)
//
//            // Re-set text to notify the listener
//            editTextContent.setMarkdownTextSilently(editTextContent.text)
//
//            isMarkwonAttachedToEditText = true
//            setMarkdownToolbarVisibility()
//        }
//    }
//
//    private fun disableMarkdownTextWatcher() = with(binding) {
//        if (markwonTextWatcher != null && isMarkwonAttachedToEditText) {
//            // TextWatcher is created and currently attached to the EditText, we detach it
//            editTextContent.removeTextChangedListener(markwonTextWatcher)
//            val text = editTextContent.text.toString()
//
//            editTextContent.text?.clearSpans()
//            editTextContent.setTextSilently(text)
//
//            isMarkwonAttachedToEditText = false
//            setMarkdownToolbarVisibility()
//        }
//    }
//
//    override fun setupToolbar(): Unit = with(binding) {
//        super.setupToolbar()
//        val onBackPressedHandler = {
//            if (findNavController().navigateUp()) {
//                // This is needed because "Notes" label briefly appears
//                // during the shared element transition when returning.
//                // Todo: Needs a better fix
//                toolbar.setTitleTextColor(Color.TRANSPARENT)
//
//                // This is needed because the view jumps around
//                // during the shared element transition when returning.
//                // Todo: Needs a better fix
//                notebookView.isVisible = false
//            }
//        }
//
//        toolbar.setNavigationOnClickListener { onBackPressedHandler() }
//        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
//            if (!onBackPressHandled) {
//                onBackPressedHandler()
//                onBackPressHandled = true
//            }
//        }
//    }
//
//    private fun addTask(position: Int = tasksAdapter.tasks.size) {
//        tasksAdapter.tasks.add(position, NoteTask(nextTaskId, "", false))
//        tasksAdapter.notifyItemInserted(tasksAdapter.tasks.size - 1)
//
//        if (position < tasksAdapter.tasks.size - 1) {
//            tasksAdapter.notifyItemRangeChanged(position, tasksAdapter.tasks.size - position + 1)
//        }
//
//        binding.recyclerTasks.doOnNextLayout {
//            (binding.recyclerTasks.findViewHolderForAdapterPosition(position) as TaskViewHolder).requestFocus()
//        }
//
//        nextTaskId += 1
//        model.updateTaskList(tasksAdapter.tasks)
//    }
//
//    private fun updateTask(position: Int, content: String? = null, isDone: Boolean? = null) {
//        tasksAdapter.tasks = tasksAdapter.tasks
//            .mapIndexed { index, task ->
//                when (index) {
//                    position -> task.copy(
//                        content = content ?: task.content,
//                        isDone = isDone ?: task.isDone
//                    )
//                    else -> task
//                }
//            }
//            .toMutableList()
//        model.updateTaskList(tasksAdapter.tasks)
//    }
//
//    private fun showColorChangeDialog() {
//        val selected = NoteColor.values().indexOf(data.note?.color).coerceAtLeast(0)
//        val dialog = BaseDialog.build(requireContext()) {
//            setTitle(getString(R.string.action_change_color))
//            setSingleChoiceItems(NoteColor.values().map { it.localizedName }.toTypedArray(), selected) { dialog, which ->
//                model.setColor(NoteColor.values()[which])
//            }
//            setPositiveButton(getString(R.string.action_done)) { dialog, which -> }
//        }
//
//        dialog.show()
//    }
//
//    private fun showRemindersDialog(note: Note) {
//        BottomSheet.show(getString(R.string.reminders), parentFragmentManager) {
//            data.note?.reminders?.forEach { reminder ->
//                val offset = ZoneId.systemDefault().rules.getOffset(Instant.now())
//                val reminderDate = LocalDateTime.ofEpochSecond(reminder.date, 0, offset)
//
//                action(reminder.name + " (${reminderDate.format(formatter)})", R.drawable.ic_bell) {
//                    EditReminderDialog.build(note.id, reminder).show(parentFragmentManager, null)
//                }
//            }
//            action(R.string.action_new_reminder, R.drawable.ic_add) {
//                EditReminderDialog.build(note.id, null).show(parentFragmentManager, null)
//            }
//        }
//    }
//
//    /** Gives the focus to the editor fields if they are empty */
//    private fun requestFocusForFields(forceFocus: Boolean = false) = with(binding) {
//        if (editTextTitle.text.isNullOrEmpty()) {
//            editTextTitle.requestFocusAndKeyboard()
//        } else {
//            if (editTextContent.text.isNullOrEmpty() || forceFocus) {
//                editTextContent.requestFocusAndKeyboard()
//            }
//        }
//    }
//
//    private fun updateEditMode(inEditMode: Boolean = model.inEditMode, note: Note? = data.note) = with(binding) {
//        // If the note is empty the fragment should open in edit mode by default
//        val noteHasEmptyContent = note?.title?.isBlank() == true || when (note?.isList) {
//            true -> note.taskList.isEmpty()
//            else -> note?.content?.isBlank() == true
//        }
//
//        model.inEditMode = (inEditMode || noteHasEmptyContent) && !isNoteDeleted
//
//        textViewTitlePreview.isVisible = !model.inEditMode
//        editTextTitle.isVisible = model.inEditMode
//
//        actionAddTask.isVisible = isList && model.inEditMode
//        recyclerTasks.doOnPreDraw {
//            for (pos in 0 until tasksAdapter.tasks.size) {
//                (recyclerTasks.findViewHolderForAdapterPosition(pos) as? TaskViewHolder)?.isEnabled = model.inEditMode
//            }
//        }
//
//        textViewContentPreview.isVisible = !model.inEditMode && !isList
//        editTextContent.isVisible = model.inEditMode && !isList
//
//        val shouldDisplayFAB = !isNoteDeleted && !noteHasEmptyContent
//        when {
//            fabChangeMode.isVisible == shouldDisplayFAB -> { /* FAB is already like it should be, no reason to animate */
//            }
//            fabChangeMode.isVisible && !shouldDisplayFAB -> fabChangeMode.hide()
//            else -> fabChangeMode.show()
//        }
//
//        fabChangeMode.setImageResource(if (model.inEditMode) R.drawable.ic_show else R.drawable.ic_pencil)
//        setMarkdownToolbarVisibility(note)
//    }
//
//    private val NoteColor.localizedName get() = getString(
//        when (this) {
//            NoteColor.Default -> R.string.default_string
//            NoteColor.Green -> R.string.preferences_color_scheme_green
//            NoteColor.Pink -> R.string.preferences_color_scheme_pink
//            NoteColor.Blue -> R.string.preferences_color_scheme_blue
//            NoteColor.Red -> R.string.preferences_color_scheme_red
//            NoteColor.Orange -> R.string.preferences_color_scheme_orange
//            NoteColor.Yellow -> R.string.preferences_color_scheme_yellow
//        }
//    )
//
//    companion object {
//        const val MARKDOWN_DIALOG_RESULT = "MARKDOWN_DIALOG_RESULT"
//    }
//}
