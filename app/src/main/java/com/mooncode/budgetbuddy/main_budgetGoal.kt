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

/**
 * A simple [Fragment] subclass.
 * Use the [main_budgetGoal.newInstance] factory method to
 * create an instance of this fragment.
 *
 * This fragment is used to set a budget goal
 */
class main_budgetGoal : Fragment() {
    private lateinit var selectedBudgetGoal: LinearLayout
    private lateinit var databaseEvent: ValueEventListener

    @ColorInt private var colPrimary = 0
    @ColorInt private var colSub = 0
    @ColorInt private var colTextPrimary = 0
    @ColorInt private var colTextSub = 0

    // Override the onCreateView method to inflate the proper view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_budget_goal, container, false)

        // Get all the elements from the view
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarBudget)
        val spinnerCalendarSelectionMode = view.findViewById<AutoCompleteTextView>(R.id.spinnerCalendarSelectionMode)
        val txtSelectedDate = view.findViewById<TextView>(R.id.txtSelectedDate)
        val txtBudgetGoal = view.findViewById<TextView>(R.id.txtBudgetGoal)

        selectedBudgetGoal = view.findViewById(R.id.selectedBudgetGoal)
        val array = arrayOf("Single", "Range", "Multiple")

        // set the calendar to range selection mode
        spinnerCalendarSelectionMode.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, array))
        spinnerCalendarSelectionMode.setText(array[0], false)

        // on change text listener
        spinnerCalendarSelectionMode.doOnTextChanged { text, start, before, count ->
            // change the calendar selection mode
            when (text.toString()) {
                "Single" -> calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
                "Range" -> calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
                "Multiple" -> calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE
            }
        }



        // set the calendar to range selection mode
        val pager = ((((calendarView.children.elementAt(0) as PagerContainer).children.first() as CustomPager).adapter) as PagerIndicatorAdapter)

        // get the color of the primary and secondary color based on Material You
        colPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, Color.BLACK)
        colSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSecondaryContainer, Color.BLACK)
        colTextPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnPrimary, Color.BLACK)
        colTextSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)

        // set the color of the pager
        pager.defaultButtonBackgroundColor = colSub
        pager.selectedButtonBackgroundColor = colPrimary
        pager.defaultButtonTextColor = colTextSub
        pager.selectedButtonTextColor = colTextPrimary


        // update the UI
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                updateLst(it)
            }


        // Store the database event listener
        // This is used to add/remove the event listener when the fragment is stopped
        // Used for updating the UI when the data from the firebase is changed
        databaseEvent = object: ValueEventListener {
            // This function is called when the data is changed
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Data changed")
                updateLst(snapshot)
            }

            // This function is called when there is an error
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }


        // function to group the dates
        calendarView.addOnRangeSelectedListener(object: OnRangeSelectedListener {
            override fun onRangeSelected(widget: MaterialCalendarView, dates: List<CalendarDay>) {
                // group the dates
                txtSelectedDate.text = dateGroup(calendarView.selectedDates.map { itt -> itt.date.time})
            }
        })

        // function to group the dates
        calendarView.addOnDateChangedListener(object: OnDateSelectedListener {
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
                // group the dates
                txtSelectedDate.text = dateGroup(calendarView.selectedDates.map { itt -> itt.date.time})
            }
        })


        // Add the on click listener to the add button
        btnAdd.setOnClickListener {
            // check if the date is empty
            if (calendarView.selectedDates.isEmpty()){
                // show an error dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid date!")
                    .setMessage("Please select a date!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()
            // check if the budget goal is empty or less than 5
            } else if (txtBudgetGoal.text.toString().isEmpty() || txtBudgetGoal.text.toString().toDouble() <= 5){
                // show an error dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid budget goal!")
                    .setMessage("Please enter budget goal!\nMinimum of ₱ 5.00")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()

            // check if selected dates has past
            } else if (calendarView.selectedDates.any { it.date.time < System.currentTimeMillis() }){
                // show an error dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid date!")
                    .setMessage("Please select a date in the future!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()

            } else {
                // add the budget goal to the database
                for (d in calendarView.selectedDates) {
                    // in every date, add the budget goal
                    databaseReference!!
                        .child(auth!!.currentUser!!.uid)
                        .child("goals")
                        .child("budget")
                        .child(d.date.time.toString())
                        .setValue(hashMapOf(
                            "price" to txtBudgetGoal.text.toString()
                        ))
                }

                // show a success dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Success!")
                    .setMessage("Saving goal added!")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()
            }
        }

        // Add the on click listener to the back button
        btnBack.setOnClickListener {
            // go back to the previous fragment
            findNavController().popBackStack()
        }

        return view

    }

    // function to show the history of the budget goals
    private fun updateLst(data: DataSnapshot){
        // check if the user is still in the fragment

        // dictionary of goals and list of dates
        val listFullfilledGoals = mutableMapOf<Double, List<Long>>()
        val listGoalUpcomingGoals = mutableMapOf<Double, List<Long>>()
        val listExceededGoal = mutableMapOf<Double, List<Long>>()


        // clear the selectedBudgetGoal
        selectedBudgetGoal.removeAllViews()
        // for the animation, set the alpha to 0
        selectedBudgetGoal.alpha = 0F

        val budget = data.child("goals").child("budget")
        val histCashOut = data.child("history").child("cashOut")

        // check if the budget goal exists
        if (budget.exists()) {
            // loop through the budget goals
            budget.children.forEach { itd ->
                val d = itd.key.toString()

                Log.d("Firebase: BudgetGoal", itd.toString())

                // add a gridlayout child on selectedBudgetGoal
                val price = data.child("goals").child("budget").child(d).child("price").value.toString().toDouble()

                // check if the date is in the history
                if (histCashOut.child(d).exists()) {
                    // check if the price is greater than the sum of the cashOut
                    if (price >= histCashOut.child(d).children.map { itt -> itt.child("value").value.toString().toDouble() }.sum()) {
                        // add the price to the existing list
                        if (listFullfilledGoals.containsKey(price))
                            listFullfilledGoals[price] = listFullfilledGoals[price]!! + listOf(d.toLong())
                        // if the price is not in the list, create a new list
                        else
                            listFullfilledGoals[price] = listOf(d.toLong())
                    } else {
                        // add the price to the existing list
                        if (listExceededGoal.containsKey(price))
                            listExceededGoal[price] = listExceededGoal[price]!! + listOf(d.toLong())
                        // if the price is not in the list, create a new list
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

            // Set the group of goals

            // For the upcoming goals label
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

            // For the upcoming goals dates
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

            // For the fullfilled goals label
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

            // For the exceeded goals label
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

            // For the exceeded goals dates
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

    // Convert the dp to float
    private fun Float.dpToFloat(): Float {
        val metrics = requireContext().resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)
    }



}