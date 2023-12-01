package com.mooncode.budgetbuddy

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
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
import java.text.SimpleDateFormat


class main_budgetGoal : Fragment() {
    private lateinit var selectedBudgetGoal: LinearLayout
    private lateinit var databaseEvent: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_budget_goal, container, false)

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)

        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarBudget)
        val spinnerCalendarSelectionMode = view.findViewById<AutoCompleteTextView>(R.id.spinnerCalendarSelectionMode)
        val txtSelectedDate = view.findViewById<TextView>(R.id.txtSelectedDate)
        val txtBudgetGoal = view.findViewById<TextView>(R.id.txtBudgetGoal)
        selectedBudgetGoal = view.findViewById<LinearLayout>(R.id.selectedBudgetGoal)

        val array = arrayOf("Single", "Range", "Multiple")

        spinnerCalendarSelectionMode.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, array))
        spinnerCalendarSelectionMode.setText(array[0], false)

        // on change text
        spinnerCalendarSelectionMode.doOnTextChanged { text, start, before, count ->
            when (text.toString()) {
                "Single" -> calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
                "Range" -> calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
                "Multiple" -> calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE
            }
        }



        // set the calendar to range selection mode
        val pager = ((((calendarView.children.elementAt(0) as PagerContainer).children.first() as CustomPager).adapter) as PagerIndicatorAdapter)

        val colPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary  , Color.BLACK)
        @ColorInt val colSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSecondaryContainer, Color.BLACK)

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





        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                updateLst(it)
            }

        databaseEvent = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Data changed")
                updateLst(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }



        calendarView.addOnRangeSelectedListener(object: OnRangeSelectedListener {
            override fun onRangeSelected(widget: MaterialCalendarView, dates: List<CalendarDay>) {
//                updateListGoals()

                txtSelectedDate.text = dateGroup(calendarView.selectedDates.map { itt -> itt.date.time})
            }
        })

        calendarView.addOnDateChangedListener(object: OnDateSelectedListener {
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
//                updateListGoals()

                txtSelectedDate.text = dateGroup(calendarView.selectedDates.map { itt -> itt.date.time})
            }
        })



        btnAdd.setOnClickListener {
            // add the goal to the database
            if (calendarView.selectedDates.isEmpty()){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid date!")
                    .setMessage("Please select a date!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()
            } else if (txtBudgetGoal.text.toString().isEmpty() || txtBudgetGoal.text.toString().toDouble() <= 5){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid budget goal!")
                    .setMessage("Please enter budget goal!\nMinimum of ₱ 5.00")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()

                // check if selected dates has past
            } else if (calendarView.selectedDates.any { it.date.time < System.currentTimeMillis() }){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid date!")
                    .setMessage("Please select a date in the future!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()

            } else {
                for (d in calendarView.selectedDates) {
                    databaseReference!!
                        .child(auth!!.currentUser!!.uid)
                        .child("goals")
                        .child("budget")
                        .child(d.date.time.toString())
                        .setValue(hashMapOf(
                            "price" to txtBudgetGoal.text.toString()
                        ))

                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Success!")
                    .setMessage("Saving goal added!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()
            }

        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        return view

    }


    private fun updateLst(data: DataSnapshot){
        // check if the user is still in the fragment

        // dictionary of goals and list of dates
        val listFullfilledGoals = mutableMapOf<Double, List<Long>>()
        val listGoalUpcomingGoals = mutableMapOf<Double, List<Long>>()
        val listExceededGoal = mutableMapOf<Double, List<Long>>()

        val colPrimary = requireContext().obtainStyledAttributes(TypedValue().data, intArrayOf(androidx.appcompat.R.attr.colorPrimary)).getColor(0, 0)

        selectedBudgetGoal.removeAllViews()

        selectedBudgetGoal.alpha = 0F


        val budget = data.child("goals").child("budget")
        val histCashOut = data.child("history").child("cashOut")

        if (budget.exists()) {
            budget.children.forEach { itd ->
                val d = itd.key.toString()

                Log.d("Firebase: BudgetGoal", itd.toString())

                // add a gridlayout child on selectedBudgetGoal
                val price = data.child("goals").child("budget").child(d).child("price").value.toString().toDouble()

                if (histCashOut.child(d).exists()) {
                    // check if the price is greater than the sum of the cashOut
                    if (price >= histCashOut.child(d).children.map { itt -> itt.child("value").value.toString().toDouble() }.sum()) {
                        if (listFullfilledGoals.containsKey(price))
                            listFullfilledGoals[price] = listFullfilledGoals[price]!! + listOf(d.toLong())
                        else
                            listFullfilledGoals[price] = listOf(d.toLong())
                    } else {
                        if (listExceededGoal.containsKey(price))
                            listExceededGoal[price] = listExceededGoal[price]!! + listOf(d.toLong())
                        else
                            listExceededGoal[price] = listOf(d.toLong())
                    }

                }
                // check if the d is in the past
                else if (d.toLong() < System.currentTimeMillis()){
                    if (listExceededGoal.containsKey(price))
                        listExceededGoal[price] = listExceededGoal[price]!! + listOf(d.toLong())
                    else
                        listExceededGoal[price] = listOf(d.toLong())

                } else {
                    if (listGoalUpcomingGoals.containsKey(price))
                        listGoalUpcomingGoals[price] =
                            listGoalUpcomingGoals[price]!! + listOf(d.toLong())
                    else
                        listGoalUpcomingGoals[price] = listOf(d.toLong())
                }


            }



            if (listGoalUpcomingGoals.isNotEmpty()){
                val txtCompleted = TextView(requireContext())
                txtCompleted.text = "Upcoming Goals"
                txtCompleted.width = 0
                txtCompleted.textSize = 20f
                // select text color
                txtCompleted.setTextColor(colPrimary)
                txtCompleted.setTypeface(null, Typeface.BOLD)
                txtCompleted.setPadding(0, 20F.dpToFloat().toInt(), 0, 0)

                selectedBudgetGoal.addView(txtCompleted)
            }

            listGoalUpcomingGoals.forEach {it2 ->
                val price = it2.key
                val date = it2.value

                val lnP = LinearLayout(requireContext())
                lnP.orientation = LinearLayout.HORIZONTAL


                val txtPrice = TextView(requireContext())
                txtPrice.text = "₱ %,.2f".format(price)
                txtPrice.textSize = 40f
                lnP.addView(txtPrice)

                val txtPriceX = TextView(requireContext())
                txtPriceX.text = " x ${date.size}"
                txtPriceX.textSize = 20f
                lnP.addView(txtPriceX)


                val txtDate = TextView(requireContext())
                txtDate.text =  dateGroup(date)
                txtDate.width = 0
                txtDate.textSize = 18f

                lnP.setPadding(0, 20, 0, 0)

                selectedBudgetGoal.addView(lnP)
                selectedBudgetGoal.addView(txtDate)
            }

            if (listFullfilledGoals.isNotEmpty()){
                val txtCompleted = TextView(requireContext())
                txtCompleted.text = "Fullfilled Goals"
                txtCompleted.width = 0
                txtCompleted.textSize = 20f
                txtCompleted.setTextColor(colPrimary)
                txtCompleted.setTypeface(null, Typeface.BOLD)
                txtCompleted.setPadding(0, 30F.dpToFloat().toInt(), 0, 0)

                selectedBudgetGoal.addView(txtCompleted)
            }

            listFullfilledGoals.forEach {it2 ->
                val price = it2.key
                val date = it2.value

                val lnP = LinearLayout(requireContext())
                lnP.orientation = LinearLayout.HORIZONTAL


                val txtPrice = TextView(requireContext())
                txtPrice.text = "₱ %,.2f".format(price)
                txtPrice.textSize = 40f
                lnP.addView(txtPrice)

                val txtPriceX = TextView(requireContext())
                txtPriceX.text = " x ${date.size}"
                txtPriceX.textSize = 20f
                lnP.addView(txtPriceX)


                val txtCashIn = TextView(requireContext())
                txtCashIn.text =  "Total cash out of ₱ %,.2f on".format(date.map { itd -> histCashOut.child(itd.toString()).children.map { itt -> itt.child("value").value.toString().toDouble() }.sum()}.sum())
                txtCashIn.width = 0
                txtCashIn.textSize = 16f

                val txtDate = TextView(requireContext())
                txtDate.text =  dateGroup(date)
                txtDate.width = 0
                txtDate.textSize = 20f

                lnP.setPadding(0, 20, 0, 0)

                selectedBudgetGoal.addView(lnP)
                selectedBudgetGoal.addView(txtCashIn)
                selectedBudgetGoal.addView(txtDate)
            }

            if (listExceededGoal.isNotEmpty()){
                val txtCompleted = TextView(requireContext())
                txtCompleted.text = "Exceeded Goals"
                txtCompleted.width = 0
                txtCompleted.textSize = 20f
                txtCompleted.setTextColor(colPrimary)
                txtCompleted.setTypeface(null, Typeface.BOLD)
                txtCompleted.setPadding(0, 30F.dpToFloat().toInt(), 0, 0)

                selectedBudgetGoal.addView(txtCompleted)
            }

            listExceededGoal.forEach {it2 ->
                val price = it2.key
                val date = it2.value

                val lnP = LinearLayout(requireContext())
                lnP.orientation = LinearLayout.HORIZONTAL


                val txtPrice = TextView(requireContext())
                txtPrice.text = "₱ %,.2f".format(price)
                txtPrice.textSize = 40f
                lnP.addView(txtPrice)

                val txtPriceX = TextView(requireContext())
                txtPriceX.text = " x ${date.size}"
                txtPriceX.textSize = 20f
                lnP.addView(txtPriceX)


                val txtCashIn = TextView(requireContext())
                txtCashIn.text =  "Total cash out of ₱ %,.2f on".format(date.map { itd -> histCashOut.child(itd.toString()).children.map { itt -> itt.child("value").value.toString().toDouble() }.sum()}.sum())
                txtCashIn.width = 0
                txtCashIn.textSize = 16f


                val txtDate = TextView(requireContext())
                txtDate.text =  dateGroup(date)
                txtDate.width = 0
                txtDate.textSize = 20f

                lnP.setPadding(0, 20, 0, 0)

                selectedBudgetGoal.addView(lnP)
                selectedBudgetGoal.addView(txtCashIn)
                selectedBudgetGoal.addView(txtDate)
            }

        }
        selectedBudgetGoal.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()

    }


    override fun onStart() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .addValueEventListener(databaseEvent )
        Log.d("Firebase: BudgetGoal", "Event listener added")
        super.onStart()
    }

    override fun onStop() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .removeEventListener(databaseEvent)

        Log.d("Firebase: BudgetGoal", "Event listener removed")
        super.onStop()
    }

    private fun Float.dpToFloat(): Float {
        val metrics = requireContext().resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)
    }



}