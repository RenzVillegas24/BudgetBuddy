package com.mooncode.budgetbuddy

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

class main_profile : Fragment() {
    private lateinit var databaseEvent: ValueEventListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_main_profile, container, false)

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnUpdateProfile = view.findViewById<Button>(R.id.btnUpdateProfile)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = view.findViewById<Button>(R.id.btnDeleteAccount)
        val btnRegisterAccount = view.findViewById<Button>(R.id.btnRegisterAccount)
        val txtFullName = view.findViewById<MaterialTextView>(R.id.txtFullName)
        val txtBirthday = view.findViewById<MaterialTextView>(R.id.txtBirthday)
        val txtAddress = view.findViewById<MaterialTextView>(R.id.txtAddress)
        val txtPhoneNumber = view.findViewById<MaterialTextView>(R.id.txtPhoneNumber)
        val txtEmail = view.findViewById<MaterialTextView>(R.id.txtEmail)
        val txtUsername = view.findViewById<MaterialTextView>(R.id.txtUsername)


        // Add fade animation on all the txt alement
        txtFullName.alpha = 0f
        txtBirthday.alpha = 0f
        txtAddress.alpha = 0f
        txtPhoneNumber.alpha = 0f
        txtEmail.alpha = 0f
        txtUsername.alpha = 0f

        // database event listener
        databaseEvent = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Data changed")

                // update the user informations
                txtFullName.text = "${snapshot.child("lastName").value.toString()}, ${snapshot.child("firstName").value.toString()} ${snapshot.child("middleName").value.toString()}"
                txtBirthday.text = SimpleDateFormat("MMMM dd, yyyy").format(snapshot.child("birthdayMillis").value.toString().toLong())
                txtAddress.text = snapshot.child("address").value.toString()
                txtPhoneNumber.text = snapshot.child("phoneNumber").value.toString()
                txtEmail.text = snapshot.child("email").value.toString()
                txtUsername.text = snapshot.child("username").value.toString()

                // fade in animation
                txtFullName.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
                txtBirthday.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
                txtAddress.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
                txtPhoneNumber.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
                txtEmail.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
                txtUsername.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }


        // go back to previous fragment
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // navigate to update profile fragment
        btnUpdateProfile.setOnClickListener {
            findNavController().navigate(R.id.action_main_profile_to_main_accountUpdate)
        }

        // navigate to change password fragment
        btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_main_profile_to_main_updatePassword)
        }

        // navigate to delete account fragment
        btnDeleteAccount.setOnClickListener {
            findNavController().navigate(R.id.action_main_profile_to_main_removeAccount)
        }

        // navigate to register fragment
        btnRegisterAccount.setOnClickListener {
            findNavController().navigate(R.id.action_main_profile_to_main_register, Bundle().apply {
                putBoolean("autoLogin", false)
                putBoolean("loggedIn", true)
            })

        }

        // logout
        btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    databaseReference!!
                        .child(auth!!.currentUser!!.uid)
                        .removeEventListener(databaseEvent)

                    auth!!.signOut()
                    findNavController().navigate(R.id.action_main_profile_to_main_login)
                }
                .setNegativeButton("No", null)
                .show()
        }

        return view


    }

    override fun onStart() {
        databaseReference!!
            .child(auth!!.currentUser!!.uid)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: Profile", "Event listener added")

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

        Log.d("Firebase: Profile", "Event listener removed")

    }

}