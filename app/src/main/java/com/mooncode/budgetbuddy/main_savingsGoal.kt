package com.mooncode.budgetbuddy

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
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
import java.util.UUID

class main_savingsGoal : Fragment() {
    private lateinit var selectedSavingsGoal: LinearLayout
    private lateinit var databaseEvent: ValueEventListener

    @ColorInt private var colPrimary = 0
    @ColorInt private var colSub = 0
    @ColorInt private var colTextPrimary = 0
    @ColorInt private var colTextSub = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_savings_goal, container, false)

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarSavings)
        val spinnerCalendarSelectionMode = view.findViewById<AutoCompleteTextView>(R.id.spinnerCalendarSelectionMode)
        val txtSelectedDate = view.findViewById<TextView>(R.id.txtSelectedDate)
        val txtSavingsGoal = view.findViewById<TextView>(R.id.txtSavingsGoal)
        selectedSavingsGoal = view.findViewById<LinearLayout>(R.id.selectedSavingsGoal)

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

        colPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary  , Color.BLACK)
        colSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSecondaryContainer, Color.BLACK)
        colTextPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnPrimary, Color.BLACK)
        colTextSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)

        // set the color of the pager
        pager.defaultButtonBackgroundColor = colSub
        pager.selectedButtonBackgroundColor = colPrimary
        pager.defaultButtonTextColor = colTextSub
        pager.selectedButtonTextColor = colTextPrimary




        // update the list of goals
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


        // same as the one in main_budgetGoal
        calendarView.addOnRangeSelectedListener(object: OnRangeSelectedListener {
            override fun onRangeSelected(widget: MaterialCalendarView, dates: List<CalendarDay>) {
                txtSelectedDate.text = dateGroup(calendarView.selectedDates.map { itt -> itt.date.time})
            }
        })

        calendarView.addOnDateChangedListener(object: OnDateSelectedListener {
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
                txtSelectedDate.text = dateGroup(calendarView.selectedDates.map { itt -> itt.date.time})
            }
        })


        // same as the one in main_budgetGoal
        btnAdd.setOnClickListener {
            // add the goal to the database
            if (calendarView.selectedDates.isEmpty()){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("No date selected")
                    .setMessage("Please select a date!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()
            } else if (txtSavingsGoal.text.toString().isEmpty() || txtSavingsGoal.text.toString().toDouble() <= 5){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("No savings goal")
                    .setMessage("Please enter savings goal!\nMinimum of ₱ 5.00")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()

                // check if selected dates has past
            } else if (calendarView.selectedDates.any { it.date.time < System.currentTimeMillis() }){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid date")
                    .setMessage("Please select a date in the future!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()

            } else {
                for (d in calendarView.selectedDates) {
                    // add the goal to the database
                    databaseReference!!
                        .child(auth!!.currentUser!!.uid)
                        .child("goals")
                        .child("savings")
                        .child(d.date.time.toString())
                        .setValue(hashMapOf(
                            "price" to txtSavingsGoal.text.toString(),
                            "isCompleted" to false
                        ))

                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Goal added")
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


    // update the list of goals
    fun updateLst(data: DataSnapshot){

        // dictionary of goals and list of dates
        val listGoalCompleted = mutableMapOf<Double, List<Long>>()
        val listUpcomingGoals = mutableMapOf<Double, List<Long>>()
        val listMissedGoal = mutableMapOf<Double, List<Long>>()

        // remove all views
        selectedSavingsGoal.removeAllViews()
        selectedSavingsGoal.alpha = 0F

        val savings = data.child("goals").child("savings")
        val histCashIn = data.child("history").child("cashIn")

        // if there are savings goal
        if (savings.exists()) {
            savings.children.forEach { itd ->
                val d = itd.key.toString()

                // add a gridlayout child on selectedSavingsGoal
                val price = savings.child(d).child("price").value.toString().toDouble()



                if (histCashIn.child(d).exists()) {
                    // if the price is lower than the total cash in
                    if (price <= histCashIn.child(d).children.map { itt -> itt.value.toString().toDouble() }.sum()) {
                        if (listGoalCompleted.containsKey(price))
                            listGoalCompleted[price] = listGoalCompleted[price]!! + listOf(d.toLong())
                        else
                            listGoalCompleted[price] = listOf(d.toLong())
                    } else {
                        if (listMissedGoal.containsKey(price))
                            listMissedGoal[price] = listMissedGoal[price]!! + listOf(d.toLong())
                        else
                            listMissedGoal[price] = listOf(d.toLong())
                    }

                }
                // check if the d is in the past
                else if (d.toLong() < System.currentTimeMillis()){
                    if (listMissedGoal.containsKey(price))
                        listMissedGoal[price] = listMissedGoal[price]!! + listOf(d.toLong())
                    else
                        listMissedGoal[price] = listOf(d.toLong())

                } else {
                    if (listUpcomingGoals.containsKey(price))
                        listUpcomingGoals[price] =
                            listUpcomingGoals[price]!! + listOf(d.toLong())
                    else
                        listUpcomingGoals[price] = listOf(d.toLong())
                }


            }

            // add the upcoming goals
            if (listUpcomingGoals.isNotEmpty()){
                val txtCompleted = TextView(requireContext())
                txtCompleted.text = "Upcoming Goals"
                txtCompleted.width = 0
                txtCompleted.textSize = 20f
                // select text color
                txtCompleted.setTextColor(colPrimary)
                txtCompleted.setTypeface(null, Typeface.BOLD)
                txtCompleted.setPadding(0, 20F.dpToFloat().toInt(), 0, 0)

                selectedSavingsGoal.addView(txtCompleted)
            }

            listUpcomingGoals.forEach {it2 ->
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
                txtDate.textSize = 20f

                lnP.setPadding(0, 20, 0, 0)

                selectedSavingsGoal.addView(lnP)
                selectedSavingsGoal.addView(txtDate)
            }

            // add the completed goals
            if (listGoalCompleted.isNotEmpty()){
                val txtCompleted = TextView(requireContext())
                txtCompleted.text = "Completed Goals"
                txtCompleted.width = 0
                txtCompleted.textSize = 20f
                txtCompleted.setTextColor(colPrimary)
                txtCompleted.setTypeface(null, Typeface.BOLD)
                txtCompleted.setPadding(0, 30F.dpToFloat().toInt(), 0, 0)

                selectedSavingsGoal.addView(txtCompleted)
            }

            listGoalCompleted.forEach {it2 ->
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
                txtCashIn.text =  "Total cash in of ₱ %,.2f on".format(date.map { itd -> histCashIn.child(itd.toString()).children.map { itt -> itt.value.toString().toDouble() }.sum()}.sum())
                txtCashIn.width = 0
                txtCashIn.textSize = 16f


                val txtDate = TextView(requireContext())
                txtDate.text =  dateGroup(date)
                txtDate.width = 0
                txtDate.textSize = 20f

                lnP.setPadding(0, 20, 0, 0)

                selectedSavingsGoal.addView(lnP)
                selectedSavingsGoal.addView(txtCashIn)
                selectedSavingsGoal.addView(txtDate)
            }

            // add the missed goals
            if (listMissedGoal.isNotEmpty()){
                val txtCompleted = TextView(requireContext())
                txtCompleted.text = "Missed Goals"
                txtCompleted.width = 0
                txtCompleted.textSize = 20f
                txtCompleted.setTextColor(colPrimary)
                txtCompleted.setTypeface(null, Typeface.BOLD)
                txtCompleted.setPadding(0, 30F.dpToFloat().toInt(), 0, 0)

                selectedSavingsGoal.addView(txtCompleted)
            }

            listMissedGoal.forEach {it2 ->
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
                txtCashIn.text =  "Total cash in of ₱ %,.2f on".format(date.map { itd -> histCashIn.child(itd.toString()).children.map { itt -> itt.value.toString().toDouble() }.sum()}.sum())
                txtCashIn.width = 0
                txtCashIn.textSize = 16f


                val txtDate = TextView(requireContext())
                txtDate.text =  dateGroup(date)
                txtDate.width = 0
                txtDate.textSize = 18f

                lnP.setPadding(0, 20, 0, 0)

                selectedSavingsGoal.addView(lnP)
                selectedSavingsGoal.addView(txtCashIn)
                selectedSavingsGoal.addView(txtDate)
            }

        }

        // animate the view
        selectedSavingsGoal.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
    }

    override fun onStart() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: SavingsGoal", "Event listener added")
        super.onStart()
    }

    override fun onStop() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .removeEventListener(databaseEvent)

        Log.d("Firebase: SavingsGoal", "Event listener removed")
        super.onStop()
    }

    private fun Float.dpToFloat(): Float {
        val metrics = requireContext().resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)
    }

}

