package com.mooncode.budgetbuddy

import android.content.res.TypedArray
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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
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


class main_cashOut : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_main_cash_out, container, false)

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnCashOut = view.findViewById<Button>(R.id.btnCashOut)
        val txtCashOut = view.findViewById<TextInputEditText>(R.id.textCashOut)
        val txtReason = view.findViewById<TextInputEditText>(R.id.textReason)

        llHistory = view.findViewById(R.id.llHistory)
        txtPhp = view.findViewById(R.id.txtPhp)
        txtSavings = view.findViewById(R.id.txtSavings)
        colPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, Color.BLACK)

        databaseEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                update(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {}
        }


        // get the current date at exact 12:00AM
        btnCashOut.setOnClickListener {
            // check if cashOut is empty
            if (txtCashOut.text.toString().isEmpty()) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("No Value")
                    .setMessage("Please enter a value to cash out.")
                    .setPositiveButton("Okay", null)
                    .setCancelable(false)
                    .show()
                return@setOnClickListener
            }
            // if cashOut is lower than 20
            else if (txtCashOut.text.toString().toDouble() < 20) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Invalid Value")
                    .setMessage("You cannot cash out less than ₱20.00.")
                    .setPositiveButton("Okay", null)
                    .setCancelable(false)
                    .show()
                return@setOnClickListener
            // if reason is empty
            } else if (txtReason.text.toString().isEmpty()) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("No Reason")
                    .setMessage("Please enter a reason for cashing out.")
                    .setPositiveButton("Okay", null)
                    .setCancelable(false)
                    .show()
                return@setOnClickListener

            }

            val budgetGoals = databaseReference!!.child(auth!!.uid!!).child("isLocked")
            // check if isLocked is present
            budgetGoals.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // if isLocked is present
                    if (snapshot.exists()) {
                        // Do not allow cash out if account is locked
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Account Locked")
                            .setMessage("You have locked your account for spending your savings. Please unlock first to continue.")
                            .setPositiveButton("Okay", null)
                            .setCancelable(false)
                            .show()

                    } else {
                        // Confirm cash out
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(
                                "Are you sure you want to cash out ₱%,.2f?".format(
                                    txtCashOut.text.toString().toDouble()
                                )
                            )
                            .setPositiveButton("Yes") { _, _ ->
                                // Set the new savings
                                val cashOut = txtCashOut.text.toString().toDouble()
                                val budget = databaseReference!!.child(auth!!.uid!!).child("money")
                                budget.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val currentBudget = snapshot.value.toString().toDouble()
                                            val newBudget = currentBudget - cashOut

                                            // check if newBudget is negative
                                            if (newBudget < 0) {
                                                MaterialAlertDialogBuilder(requireActivity())
                                                    .setTitle("Insufficient Funds")
                                                    .setMessage(
                                                        "You do not have enough money to cash out ₱%,.2f.\nYour current money is ₱%,.2f".format(
                                                            cashOut,
                                                            currentBudget
                                                        )
                                                    )
                                                    .setPositiveButton("Okay", null)
                                                    .setCancelable(false)
                                                    .show()
                                                return

                                            } else {
                                                budget.setValue(newBudget)

                                                // get the current date at exact 12:00AM
                                                val current =
                                                    LocalDateTime.now().with(LocalTime.MIDNIGHT)
                                                        .atZone(ZoneId.systemDefault()).toInstant()
                                                        .toEpochMilli()

                                                val currentTime =
                                                    LocalDateTime.now().atZone(
                                                        ZoneId.systemDefault()
                                                    ).toInstant().toEpochMilli()

                                                // add to history
                                                databaseReference!!.child(auth!!.uid!!)
                                                    .child("history").child("cashOut")
                                                    .child(current.toString())
                                                    .child(currentTime.toString())
                                                    .setValue(hashMapOf(
                                                        "value" to cashOut,
                                                        "reason" to txtReason.text.toString()
                                                    ))
                                                    .addOnSuccessListener {
                                                        MaterialAlertDialogBuilder(requireActivity())
                                                            .setTitle("Cash Out")
                                                            .setMessage(
                                                                "You have successfully cashed out ₱%,.2f, with the reason of: \"%s\"\nYour current money is ₱%,.2f".format(
                                                                    cashOut,
                                                                    txtReason.text.toString(),
                                                                    newBudget
                                                                )
                                                            )
                                                            .setPositiveButton("Okay") { _, _ ->
                                                                txtCashOut.text!!.clear()
                                                                txtReason.text!!.clear()
                                                            }
                                                            .setCancelable(false)
                                                            .show()
                                                    }
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

        // go back to main
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

        Log.d("Firebase: CashOut", "Event Listener added")

        super.onStart()
    }

    override fun onStop() {
        databaseReference!!
            .child(auth!!.uid!!)
            .removeEventListener(databaseEvent)

        Log.d("Firebase: CashOut", "Event Listener removed")

        super.onStop()
    }


    // update the savings and history
    fun update(it: DataSnapshot){
        // update savings
        txtSavings.text = "%,.2f".format(it.child("money").value.toString().toDouble())
        txtSavings.animate().alpha(1f).setDuration(500).setInterpolator(
            AccelerateInterpolator()
        ).start()
        // animate savings, fade in
        txtPhp.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()

        // update history
        llHistory.removeAllViews()
        llHistory.alpha = 0f

        it.child("history").child("cashOut").children.forEach {ith ->
            val histDate = ith.key.toString().toLong()

            val histDateView = TextView(requireActivity())
            histDateView.text = SimpleDateFormat("MMMM dd, yyyy").format(histDate)
            histDateView.textSize = 25f
            histDateView.setTextColor(colPrimary)
            histDateView.setPadding(0, 40, 0, 0)
            llHistory.addView(histDateView)


            ith.children.forEach{itdt ->
                val histTime = itdt.key.toString().toLong()
                val histValue = itdt.child("value").value.toString().toDouble()
                val histReason = itdt.child("reason").value.toString()

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


                val histReasonView = TextView(requireActivity())
                histReasonView.text = "\"$histReason\""
                histReasonView.textSize = 18f
                histReasonView.typeface = Typeface.create("sans-serif", Typeface.ITALIC)

                llHistory.addView(llTm)
                llHistory.addView(histReasonView)

                // animate history, fade in
                llHistory.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()



            }

        }

    }


}