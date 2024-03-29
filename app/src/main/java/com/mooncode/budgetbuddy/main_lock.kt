package com.mooncode.budgetbuddy

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
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
    private var colTextPrimary by Delegates.notNull<Int>()
    private var colTextSub by Delegates.notNull<Int>()
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
        val btnBack = view.findViewById<MaterialButton>(R.id.btnBack)

        var isLocked = false

        colPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary  , Color.BLACK)
        colSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSecondaryContainer, Color.BLACK)
        colTextPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnPrimary, Color.BLACK)
        colTextSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)
        val pager = ((((calendarSetLock.children.elementAt(0) as PagerContainer).children.first() as CustomPager).adapter) as PagerIndicatorAdapter)

        // set the color of the pager
        pager.defaultButtonBackgroundColor = colSub
        pager.selectedButtonBackgroundColor = colPrimary
        pager.defaultButtonTextColor = colTextSub
        pager.selectedButtonTextColor = colTextPrimary

        // configure the calendar
        calendarSetLock.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
        calendarSetLock.selectRange(CalendarDay.today(), CalendarDay.today())
        calendarSetLock.state()?.edit()?.setMinimumDate(CalendarDay.today())?.commit()
        calendarSetLock.addOnDateChangedListener(object: OnDateSelectedListener {
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
                calendarSetLock.selectRange(CalendarDay.today(), CalendarDay.today())
            }
        })

        // if the user selects a date range, update the text
        calendarSetLock.addOnRangeSelectedListener(object: OnRangeSelectedListener {
            override fun onRangeSelected(widget: MaterialCalendarView, dates: List<CalendarDay>) {
                txtSelectedDate.text = dateGroup(calendarSetLock.selectedDates.map { itt -> itt.date.time})
                txtSelectedDateInf.text = "Your account will be locked for ${calendarSetLock.selectedDates.size} day${if (calendarSetLock.selectedDates.size == 1) "" else "s" }."
            }
        })

        // set the lock button
        btnLock.setOnClickListener {
            // if the account is locked, ask the user if they want to unlock it
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
            // if the account is not locked, ask the user if they want to lock it
            else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Lock Account")
                    .setMessage("Are you sure you want to lock your account for ${calendarSetLock.selectedDates.size} day${if (calendarSetLock.selectedDates.size == 1) "" else "s" }?")
                    .setPositiveButton("Yes") { _, _ ->
                        // set the lock
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


        // set the back button
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // set the database event listener
        databaseEvent = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Data changed")

                // if the user is locked, update the text
                if (snapshot.child("isLocked").exists()){
                    val from = snapshot.child("isLocked").child("from").value as Long
                    val to = snapshot.child("isLocked").child("to").value as Long

                    if (from == to || System.currentTimeMillis() in from..to){
                        // if the date is in the future, set the lock
                        txtLockTitle.text = "Unlock"
                        btnLock.text = "Unlock Savings"
                        txtSelectedDateTitle.visibility = View.GONE
                        txtSelectedDate.visibility = View.GONE

                        calendarSetLock.selectionMode = MaterialCalendarView.SELECTION_MODE_NONE
                        calendarSetLock.selectRange(CalendarDay.from(Date(from)), CalendarDay.from(Date(to)))
                        txtSelectedDateInf.text = "Your account is locked for ${calendarSetLock.selectedDates.size} day${if (calendarSetLock.selectedDates.size == 1) "" else "s" } until ${dateGroup(listOf(to))}."

                        isLocked = true
                    } else {
                        // if the date has passed, remove the lock
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