package com.mooncode.budgetbuddy

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shuhart.materialcalendarview.CalendarDay
import com.shuhart.materialcalendarview.MaterialCalendarView
import com.shuhart.materialcalendarview.OnDateSelectedListener
import com.shuhart.materialcalendarview.OnRangeSelectedListener
import com.shuhart.materialcalendarview.indicator.pager.CustomPager
import com.shuhart.materialcalendarview.indicator.pager.PagerContainer
import com.shuhart.materialcalendarview.indicator.pager.PagerIndicatorAdapter
import java.time.Instant
import java.util.Calendar
import java.util.Date
import kotlin.properties.Delegates


class main_lock : Fragment() {
    private var colPrimary by Delegates.notNull<Int>()
    private var colSub by Delegates.notNull<Int>()
    private lateinit var databaseEvent: ValueEventListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view  =  inflater.inflate(R.layout.fragment_main_lock, container, false)
        val txtSelectedDate = view.findViewById<TextView>(R.id.txtSelectedDate)
        val txtSelectedDateInf = view.findViewById<TextView>(R.id.txtSelectedDateInf)
        val txtSelectedDateTitle = view.findViewById<TextView>(R.id.txtSelectedDateTitle)
        val txtLockTitle = view.findViewById<TextView>(R.id.txtLockTitle)
        val calendarSetLock = view.findViewById<MaterialCalendarView>(R.id.calendarSetLock)
        val btnLock = view.findViewById<TextView>(R.id.btnLock)

        var isLocked = false

        colPrimary = requireContext().obtainStyledAttributes(TypedValue().data, intArrayOf(androidx.appcompat.R.attr.colorPrimary)).getColor(0, 0)
        colSub = resources.getColor(R.color.mcv_selectionRangeColor)


        val pager = ((((calendarSetLock.children.elementAt(0) as PagerContainer).children.first() as CustomPager).adapter) as PagerIndicatorAdapter)

        pager.defaultButtonBackgroundColor = colSub
        pager.selectedButtonBackgroundColor = colPrimary

        // check if the window is in light or dark mode
        when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                pager.defaultButtonTextColor = resources.getColor(com.shuhart.materialcalendarview.R.color.mcv_text_date_dark)
                pager.selectedButtonTextColor = resources.getColor(com.shuhart.materialcalendarview.R.color.mcv_text_date_light)
            }
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                pager.defaultButtonTextColor = resources.getColor(com.shuhart.materialcalendarview.R.color.mcv_text_date_light)
                pager.selectedButtonTextColor = resources.getColor(com.shuhart.materialcalendarview.R.color.mcv_text_date_dark)
            }
        }

        calendarSetLock.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
        calendarSetLock.selectRange(CalendarDay.today(), CalendarDay.today())
        calendarSetLock.state()?.edit()?.setMinimumDate(CalendarDay.today())?.commit()
        calendarSetLock.addOnDateChangedListener(object: OnDateSelectedListener {
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
                calendarSetLock.selectRange(CalendarDay.today(), CalendarDay.today())
            }
        })

        calendarSetLock.addOnRangeSelectedListener(object: OnRangeSelectedListener {
            override fun onRangeSelected(widget: MaterialCalendarView, dates: List<CalendarDay>) {
                txtSelectedDate.text = dateGroup(calendarSetLock.selectedDates.map { itt -> itt.date.time})
                txtSelectedDateInf.text = "Your account will be locked for ${calendarSetLock.selectedDates.size} day${if (calendarSetLock.selectedDates.size == 1) "" else "s" }."
            }
        })

        btnLock.setOnClickListener {

            if (isLocked){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Unlock Account")
                    .setMessage("Are you sure you want to unlock your account?")
                    .setPositiveButton("Yes") { _, _ ->
                        databaseReference!!
                            .child(auth!!.currentUser!!.uid)
                            .child("isLocked")
                            .removeValue()
                    }
                    .setNegativeButton("No", null)
                    .setCancelable(false)
                    .show()
            }
            else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Lock Account")
                    .setMessage("Are you sure you want to lock your account for ${calendarSetLock.selectedDates.size} day${if (calendarSetLock.selectedDates.size == 1) "" else "s" }?")
                    .setPositiveButton("Yes") { _, _ ->
                       databaseReference!!
                           .child(auth!!.currentUser!!.uid)
                           .child("isLocked")
                           .setValue(hashMapOf(
                               "from" to calendarSetLock.selectedDates.first().date.time,
                                "to" to calendarSetLock.selectedDates.last().date.time))
                    }
                    .setNegativeButton("No", null)
                    .setCancelable(false)
                    .show()
            }

        }

        databaseEvent = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Data changed")

                if (snapshot.child("isLocked").exists()){
                    val from = snapshot.child("isLocked").child("from").value as Long
                    val to = snapshot.child("isLocked").child("to").value as Long


                    if (from == to || System.currentTimeMillis() in from..to){
                        txtLockTitle.text = "Unlock"
                        btnLock.text = "Unlock Savings"
                        txtSelectedDateTitle.visibility = View.GONE
                        txtSelectedDate.visibility = View.GONE

                        calendarSetLock.selectionMode = MaterialCalendarView.SELECTION_MODE_NONE
                        calendarSetLock.selectRange(CalendarDay.from(Date(from)), CalendarDay.from(Date(to)))
                        txtSelectedDateInf.text = "Your account is locked for ${calendarSetLock.selectedDates.size} day${if (calendarSetLock.selectedDates.size == 1) "" else "s" } until ${dateGroup(listOf(to))}."

                        isLocked = true
                    } else {
                        databaseReference!!
                            .child(auth!!.currentUser!!.uid)
                            .child("isLocked")
                            .removeValue()
                    }
                } else {
                    isLocked = false
                    txtLockTitle.text = "Lock"
                    btnLock.text = "Lock Savings"
                    txtSelectedDateInf.text = "Your account will be locked for ${calendarSetLock.selectedDates.size} day${if (calendarSetLock.selectedDates.size == 1) "" else "s" }."
                    txtSelectedDate.text = dateGroup(calendarSetLock.selectedDates.map { itt -> itt.date.time})
                    txtSelectedDateTitle.visibility = View.VISIBLE
                    txtSelectedDate.visibility = View.VISIBLE

                    calendarSetLock.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
                    calendarSetLock.selectRange(CalendarDay.today(), CalendarDay.today())

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }

        return view
    }

    override fun onStart() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: Lock", "Event listener added")
        super.onStart()
    }

    override fun onStop() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .removeEventListener(databaseEvent)

        Log.d("Firebase: Lock", "Event listener removed")
        super.onStop()
    }


}