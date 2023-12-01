package com.mooncode.budgetbuddy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.time.LocalDateTime


class main_menu : Fragment() {
    private lateinit var txtPhp: MaterialTextView
    private lateinit var txtSavings: MaterialTextView
    private lateinit var databaseEvent: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        txtPhp = view.findViewById<MaterialTextView>(R.id.txtPhp)
        txtSavings = view.findViewById<MaterialTextView>(R.id.txtSavings)
        val btnCashIn = view.findViewById<Button>(R.id.btnCashIn)
        val btnCashOut = view.findViewById<Button>(R.id.btnCashOut)
        val btnCalendarGoal = view.findViewById<Button>(R.id.btnCalendarGoal)
        val btnBudgetGoal = view.findViewById<Button>(R.id.btnBudgetGoal)
        val btnLock = view.findViewById<Button>(R.id.btnLock)
        val btnTransfer = view.findViewById<Button>(R.id.btnTransfer)
        val btnProfile = view.findViewById<Button>(R.id.btnProfile)


        databaseEvent =  object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Data changed")
                txtSavings.text = "%,.2f".format(snapshot.child("money").value.toString().toDouble())
                txtSavings.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
                txtPhp.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()

            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }



        btnCashIn.setOnClickListener {
            findNavController().navigate(R.id.action_main_menu_to_main_cashIn)
        }
        btnCashOut.setOnClickListener {
            findNavController().navigate(R.id.action_main_menu_to_main_cashOut)
        }
        btnCalendarGoal.setOnClickListener {
            findNavController().navigate(R.id.action_main_menu_to_main_calendarGoal)
        }
        btnBudgetGoal.setOnClickListener {
            findNavController().navigate(R.id.action_main_menu_to_main_budgetGoal)
        }
        btnLock.setOnClickListener {
            findNavController().navigate(R.id.action_main_menu_to_main_lock)
        }

        btnTransfer.setOnClickListener {
            findNavController().navigate(R.id.action_main_menu_to_main_transfer)
        }

        btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_main_menu_to_main_profile)
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle("Confirm Exit")
                        .setMessage("Are you sure you want to leave?")
                        .setPositiveButton("Leave") { dialog, which ->
                            requireActivity().finishAndRemoveTask()
                        }
                        .setNegativeButton("Cancel", null)
                        .setCancelable(false)
                        .show()
                }
            })




        return view
    }


    override fun onStart() {
        txtPhp.alpha = 0f
        txtSavings.alpha = 0f

        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: Menu", "Event listener added")

        super.onStart()
    }

    override fun onStop() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .removeEventListener(databaseEvent)
        super.onStop()

        Log.d("Firebase: Menu", "Event listener removed")
    }

}