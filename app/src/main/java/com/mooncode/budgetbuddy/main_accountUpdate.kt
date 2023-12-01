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

/**
 * A simple [Fragment] subclass.
 * Use the [main_accountUpdate.newInstance] factory method to
 * create an instance of this fragment.
 *
 * This fragment is used to update the user's profile
 */
class main_accountUpdate : Fragment() {
    private lateinit var databaseEvent: ValueEventListener

    // Override the onCreateView method to inflate the proper view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_account_update, container, false)

        // Get all the elements from the view
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

        // Stores the selected birthday
        var selected_birthDay: Long? = null


        // Store the database event listener
        // This is used to add/remove the event listener when the fragment is stopped
        // Used for updating the UI when the data from the firebase is changed
        databaseEvent = object: ValueEventListener {
            // This is called when the data in the database is changed
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

            // This is called when there is an error in the database
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }

        // Set the onClickListener for the birthday text field
        // Show the date picker when the text field is clicked
        txtBirthday.setOnClickListener {
            it as TextInputEditText

            val constraint = CalendarConstraints.Builder()
            val calendar = Calendar.getInstance()

            // Set the constraints for the date picker

            // The user must be at least 13 years old
            calendar.add(Calendar.YEAR, -13)
            constraint.setEnd(calendar.timeInMillis)
            constraint.setOpenAt(if (it.text.isNullOrBlank()) calendar.timeInMillis else selected_birthDay!!)

            // The user must be at most 150 years old
            calendar.add(Calendar.YEAR, -137)
            constraint.setStart(calendar.timeInMillis)

            // Create the date picker
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraint.build())
                .setTitleText("Select your birthday")

            // Set the selected date if the text field is not empty
            if (!it.text.isNullOrBlank())
                datePicker.setSelection(selected_birthDay)


            val datePickerBuilder = datePicker.build()

            datePickerBuilder.show(requireActivity().supportFragmentManager, "DatePicker")

            // Set the selected date when the user clicks the positive button
            datePickerBuilder.addOnPositiveButtonClickListener {it2 ->
                val date = Date(it2)
                val cal = Calendar.getInstance()
                cal.time = date

                selected_birthDay = cal.timeInMillis
                it.setText(SimpleDateFormat("MMMM d, yyyy").format(date))
            }
        }

        // Set the onClickListener for the register button
        btnRegister.setOnClickListener {
            btnBack.isEnabled = false
            btnRegister.isEnabled = false

            // Check if the user has filled up all the fields
            if (txtFirstName.text.isNullOrBlank() ||
                txtMiddleName.text.isNullOrBlank() ||
                txtLastName.text.isNullOrBlank() ||
                selected_birthDay == null ||
                txtAddress.text.isNullOrBlank() ||
                txtPhoneNumber.text.isNullOrBlank() ||
                txtEmail.text.isNullOrBlank() ||
                txtPassword.text.isNullOrBlank() ||
                txtConfPassword.text.isNullOrBlank()) {

                // Show an error message if the user has not filled up all the fields
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
                // Check if the password and confirmation password matches
                if (txtPassword.text.toString() != txtConfPassword.text.toString()) {
                    // Show an error message if the password and confirmation password does not match
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

                // Update the user's profile
                // This is done by updating the data in the firebase database
                databaseReference!!
                    // Get the current user's id
                    .child(auth!!.currentUser!!.uid)
                    // Update the user's data
                    .updateChildren(
                        // Create a map of the user's data
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
                    // Add a success listener
                    .addOnSuccessListener {
                        // Show a success message if the update is successful
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
                    // Add a failure listener
                    .addOnFailureListener {
                        // Show an error message if the update is not successful
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

        // Set the onClickListener for the back button
        btnBack.setOnClickListener {
            // Go back to the previous fragment
            findNavController().popBackStack()
        }


        return view
    }

    // Every time the fragment is started, add the event listener
    // This is implemented in every fragment that uses the firebase database
    override fun onStart() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: UpdateProfile", "Event listener added")

        super.onStart()
    }

    // Every time the fragment is stopped, remove the event listener
    // This is implemented in every fragment that uses the firebase database
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