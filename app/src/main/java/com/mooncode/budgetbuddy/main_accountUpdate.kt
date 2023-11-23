package com.mooncode.budgetbuddy

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date


class main_accountUpdate : Fragment() {
    private lateinit var databaseEvent: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_account_update, container, false)

        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val txtFirstName = view.findViewById<TextInputEditText>(R.id.txtFirstName)
        val txtMiddleName = view.findViewById<TextInputEditText>(R.id.txtMiddleInitial)
        val txtLastName = view.findViewById<TextInputEditText>(R.id.txtLastName)
        val txtBirthday = view.findViewById<TextInputEditText>(R.id.txtBirthday)
        val txtAddress = view.findViewById<TextInputEditText>(R.id.txtAddress)
        val txtPhoneNumber = view.findViewById<TextInputEditText>(R.id.txtPhoneNumber)
        val txtEmail = view.findViewById<TextInputEditText>(R.id.txtEmail)
        val txtPassword = view.findViewById<TextInputEditText>(R.id.txtPassword)
        val txtConfPassword = view.findViewById<TextInputEditText>(R.id.txtConfPassword)

        var selected_birthDay: Long? = null


        databaseEvent = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Data changed")

                txtFirstName.setText(snapshot.child("firstName").value.toString())
                txtMiddleName.setText(snapshot.child("middleName").value.toString())
                txtLastName.setText(snapshot.child("lastName").value.toString())
                txtBirthday.setText(SimpleDateFormat("MMMM d, yyyy").format(snapshot.child("birthdayMillis").value.toString().toLong()))
                selected_birthDay = snapshot.child("birthdayMillis").value.toString().toLong()
                txtAddress.setText(snapshot.child("address").value.toString())
                txtPhoneNumber.setText(snapshot.child("phoneNumber").value.toString())
                txtEmail.setText(snapshot.child("email").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }


        txtBirthday.setOnClickListener {
            it as TextInputEditText

            val constraint = CalendarConstraints.Builder()
            val calendar = Calendar.getInstance()

            calendar.add(Calendar.YEAR, -13)
            constraint.setEnd(calendar.timeInMillis)

            constraint.setOpenAt(if (it.text.isNullOrBlank()) calendar.timeInMillis else selected_birthDay!!)


            calendar.add(Calendar.YEAR, -137)
            constraint.setStart(calendar.timeInMillis)


            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraint.build())
                .setTitleText("Select your birthday")


            if (!it.text.isNullOrBlank())
                datePicker.setSelection(selected_birthDay)


            val datePickerBuilder = datePicker.build()

            datePickerBuilder.show(requireActivity().supportFragmentManager, "DatePicker")

            datePickerBuilder.addOnPositiveButtonClickListener {it2 ->
                val date = Date(it2)
                val cal = Calendar.getInstance()
                cal.time = date

                selected_birthDay = cal.timeInMillis
                it.setText(SimpleDateFormat("MMMM d, yyyy").format(date))
            }
        }

        btnRegister.setOnClickListener {

            btnBack.isEnabled = false
            btnRegister.isEnabled = false

            if (txtFirstName.text.isNullOrBlank() ||
                txtMiddleName.text.isNullOrBlank() ||
                txtLastName.text.isNullOrBlank() ||
                selected_birthDay == null ||
                txtAddress.text.isNullOrBlank() ||
                txtPhoneNumber.text.isNullOrBlank() ||
                txtEmail.text.isNullOrBlank() ||
                txtPassword.text.isNullOrBlank() ||
                txtConfPassword.text.isNullOrBlank()) {

                MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Please fill up all the fields")
                    .setPositiveButton("Try Again"){ _, _ ->
                        btnBack.isEnabled = true
                        btnRegister.isEnabled = true
                    }
                    .setCancelable(false)
                    .show()

                return@setOnClickListener
            } else {

                if (txtPassword.text.toString() != txtConfPassword.text.toString()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Password and Confirmation Password does not match")
                        .setPositiveButton("Try Again"){ _, _ ->
                            btnBack.isEnabled = true
                            btnRegister.isEnabled = true
                        }
                        .setCancelable(false)
                        .show()

                    return@setOnClickListener
                }

                databaseReference!!
                    .child(auth!!.currentUser!!.uid)
                    .updateChildren(
                        hashMapOf(
                        "firstName" to txtFirstName.text.toString(),
                        "middleName" to txtMiddleName.text.toString(),
                        "lastName" to txtLastName.text.toString(),
                        "birthday" to selected_birthDay,
                        "address" to txtAddress.text.toString(),
                        "phoneNumber" to txtPhoneNumber.text.toString(),
                        "email" to txtEmail.text.toString(),
                        "password" to txtPassword.text.toString()).toMap()
                    )
                    .addOnSuccessListener {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Account Update")
                            .setMessage("Account successfully updated!")
                            .setPositiveButton("Okay") { _, _ ->
                                findNavController().popBackStack()
                            }
                            .setCancelable(false)
                            .show()

                        Log.d("update", "success")
                    }
                    .addOnFailureListener {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Account Update")
                            .setMessage("Account creation failed. Please try again.")
                            .setPositiveButton("Try Again") { _, _ ->
                                btnBack.isEnabled = true
                                btnRegister.isEnabled = true
                            }
                            .setCancelable(false)
                            .show()

                        Log.d("update", "failed")
                    }



            }



        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }




        return view
    }

    override fun onStart() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: UpdateProfile", "Event listener added")

        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        if (auth!!.currentUser == null) {
            return
        }
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .removeEventListener(databaseEvent)

        Log.d("Firebase: UpdateProfile", "Event listener removed")

    }

}