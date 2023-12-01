package com.mooncode.budgetbuddy

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.properties.Delegates

/*
 * This fragment is used to cash in money to the user's account.
 */
class main_cashIn : Fragment() {
    private lateinit var txtPhp: MaterialTextView
    private lateinit var txtSavings: MaterialTextView
    private lateinit var llHistory: LinearLayout
    private var colPrimary by Delegates.notNull<Int>()
    private lateinit var databaseEvent: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_main_cash_in, container, false)

        var btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnCashIn = view.findViewById<Button>(R.id.btnCashIn)
        val txtCashIn = view.findViewById<TextInputEditText>(R.id.textCashIn)

        // Used to show the current money of the user
        txtPhp = view.findViewById(R.id.txtPhp)
        txtSavings = view.findViewById(R.id.txtSavings)
        llHistory = view.findViewById(R.id.llHistory)

        colPrimary = requireContext().obtainStyledAttributes(TypedValue().data, intArrayOf(androidx.appcompat.R.attr.colorPrimary)).getColor(0, 0)

        databaseEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                update(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        // Used to cash in money to the user's account
        btnCashIn.setOnClickListener {
            // check if cashIn is empty
            if (txtCashIn.text.toString().isEmpty()) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Empty Value!")
                    .setMessage("Please enter a value to cash in.")
                    .setPositiveButton("Okay", null)
                    .setCancelable(false)
                    .show()
                return@setOnClickListener
            }
            // if cashIn is lower than 20
            else if (txtCashIn.text.toString().toDouble() < 1) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Invalid Value!")
                    .setMessage("You cannot cash in less than ₱1.")
                    .setPositiveButton("Okay", null)
                    .setCancelable(false)
                    .show()
                return@setOnClickListener
            }

            val budgetGoals = databaseReference!!.child(auth!!.uid!!).child("isLocked")
            // check if isLocked is present
            budgetGoals.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // if isLocked is present and is equal to true
                    if (snapshot.exists() && snapshot.child("type").value.toString() == "all") {
                        // Do not allow the user to cash in
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Account Locked")
                            .setMessage("You have locked your account so you cannot proceed. Please unlock your account first.")
                            .setPositiveButton("Okay", null)
                            .setCancelable(false)
                            .show()
                    // if isLocked is present and is equal to false
                    } else {
                        // Ask the user if they are sure to cash in
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Cash In")
                            .setMessage("Are you sure you want to cash in ₱%,.2f?".format(txtCashIn.text.toString().toDouble()))
                            .setPositiveButton("Yes") { _, _ ->
                                // Set the new savings
                                val cashIn = txtCashIn.text.toString().toDouble()
                                val budget = databaseReference!!.child(auth!!.uid!!).child("money")
                                budget.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val currentBudget = snapshot.value.toString().toDouble()
                                            val newBudget = currentBudget + cashIn

                                            budget.setValue(newBudget)

                                            // get the current date at exact 12:00AM
                                            val current =
                                                LocalDateTime.now().with(LocalTime.MIDNIGHT).atZone(
                                                    ZoneId.systemDefault()
                                                ).toInstant().toEpochMilli()

                                            val currentTime =
                                                LocalDateTime.now().atZone(
                                                    ZoneId.systemDefault()
                                                ).toInstant().toEpochMilli()

                                            // add to history
                                            databaseReference!!.child(auth!!.uid!!).child("history")
                                                .child("cashIn")
                                                .child(current.toString())
                                                .child(currentTime.toString())
                                                .setValue(cashIn)
                                                .addOnSuccessListener {
                                                    MaterialAlertDialogBuilder(requireActivity())
                                                        .setTitle("Cash In Successful")
                                                        .setMessage("You have successfully cashed in ₱%,.2f\nYour current money is ₱%,.2f".format(cashIn, newBudget))
                                                        .setPositiveButton("Okay") { _, _ ->
                                                            txtCashIn.text!!.clear()
                                                        }
                                                        .setCancelable(false)
                                                        .show()
                                                }

                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                            .setNegativeButton("No", null)
                            .setCancelable(false)
                            .show()


                    }
                }
                override fun onCancelled(error: DatabaseError) { }
            })

        }

        // Used to go back to the previous fragment
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        return view

    }

    override fun onStart() {
        txtPhp.alpha = 0f
        txtSavings.alpha = 0f

        databaseReference!!
            .child(auth!!.uid!!)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: CashIn", "Event Listener added")

        super.onStart()
    }

    override fun onStop() {
        databaseReference!!
            .child(auth!!.uid!!)
            .removeEventListener(databaseEvent)

        Log.d("Firebase: CashIn", "Event Listener removed")

        super.onStop()
    }

    // Used to update the UI of the fragment every time the data is changed
    fun update(it: DataSnapshot){
        // Update the current money of the user
        txtSavings.text = "%,.2f".format(it.child("money").value.toString().toDouble())
        txtSavings.animate().alpha(1f).setDuration(500).setInterpolator(
            AccelerateInterpolator()
        ).start()
        // Animate the text, fade in
        txtPhp.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()

        // Update the history of the user
        llHistory.removeAllViews()
        llHistory.alpha = 0f

        it.child("history").child("cashIn").children.forEach {ith ->
            val histDate = ith.key.toString().toLong()

            val histDateView = TextView(requireActivity())
            histDateView.text = SimpleDateFormat("MMMM dd, yyyy").format(histDate)
            histDateView.textSize = 25f
            histDateView.setTextColor(colPrimary)
            histDateView.setPadding(0, 40, 0, 0)
            llHistory.addView(histDateView)


            ith.children.forEach{itdt ->
                val histTime = itdt.key.toString().toLong()
                val histValue = itdt.value.toString().toDouble()

                val llTm = LinearLayout(requireActivity())
                llTm.orientation = LinearLayout.HORIZONTAL
                llTm.setPadding(0, 10, 0, 0)

                val histTimeView = TextView(requireActivity())
                histTimeView.text = SimpleDateFormat("hh:mm a").format(histTime)
                histTimeView.textSize = 20f
                histTimeView.typeface = Typeface.create("sans-serif", Typeface.BOLD)
                histTimeView.setPadding(0, 0, 20, 0)
                llTm.addView(histTimeView)

                val histValueView = TextView(requireActivity())
                histValueView.text = "• ₱ %,.2f".format(histValue)
                histValueView.textSize = 20f
                llTm.addView(histValueView)

                llHistory.addView(llTm)

                // Animate the history, fade in
                llHistory.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()


            }

        }
    }


}