package com.ultraone.nottie.util.views

import android.R
import android.view.LayoutInflater
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView


//class UACalendarView {
//    var header: LinearLayout? = null
//    var btnToday: Button? = null
//    var btnPrev: ImageView? = null
//    var btnNext: ImageView? = null
//    var txtDateDay: TextView? = null
//    var txtDisplayDate: TextView? = null
//    var txtDateYear: TextView? = null
//    var gridView: GridView? = null
//
//    fun CalendarView(context: Context, attrs: AttributeSet) {
//        super(context, attrs)
//        initControl(context, attrs)
//    }
//
//    private fun assignUiElements() {
//        // layout is inflated, assign local variables to components
//        header = findViewById(R.id.calendar_header)
//        btnPrev = findViewById(R.id.calendar_prev_button)
//        btnNext = findViewById(R.id.calendar_next_button)
//        txtDateDay = findViewById(R.id.date_display_day)
//        txtDateYear = findViewById(R.id.date_display_year)
//        txtDisplayDate = findViewById(R.id.date_display_date)
//        btnToday = findViewById(R.id.date_display_today)
//        gridView = findViewById(R.id.calendar_grid)
//    }
//
//    /**
//     * Load control xml layout
//     */
//    private fun initControl(context: Context, attrs: AttributeSet) {
//        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        inflater.inflate(R.layout.custom_calendar, this)
//        assignUiElements()
//    }
//
//}