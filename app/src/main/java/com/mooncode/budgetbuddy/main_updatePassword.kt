package com.mooncode.budgetbuddy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.EmailAuthProvider

class main_updatePassword : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_update_password, container, false)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnUpdate = view.findViewById<View>(R.id.btnUpdate)
        val txtCurrentPassword = view.findViewById<TextInputEditText>(R.id.txtCurrentPassword)
        val txtNewPassword = view.findViewById<TextInputEditText>(R.id.txtNewPassword)
        val txtConfNewPassword = view.findViewById<TextInputEditText>(R.id.txtConfNewPassword)

        // Get database reference
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        // Get database reference
        btnUpdate.setOnClickListener {
            btnUpdate.isEnabled = false
            btnBack.isEnabled = false
            // Check if passwords match
            if (txtNewPassword.text.toString() == txtConfNewPassword.text.toString()) {
                databaseReference!!
                    .child(auth!!.currentUser!!.uid)
                    .child("password")
                    .get()
                    .addOnSuccessListener {
                        if (it.value.toString() != txtCurrentPassword.text.toString()) {
                            // Current password is incorrect
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Error")
                                .setMessage("Current password is incorrect")
                                .setPositiveButton("OK") { dialog, which ->
                                    dialog.dismiss()
                                    btnUpdate.isEnabled = true
                                    btnBack.isEnabled = true
                                }
                                .show()
                        } else {
                            // Current password is correct
                            val credential = EmailAuthProvider.getCredential(
                                auth!!.currentUser!!.email.toString(),
                                txtCurrentPassword.text.toString())

                            // Re-authenticate user
                            auth!!.currentUser!!.reauthenticate(credential)
                                .addOnCompleteListener {task ->
                                    if (task.isSuccessful){
                                        // Update password in authentication
                                        auth!!.currentUser!!.updatePassword(
                                            txtNewPassword.text.toString())
                                            .addOnCompleteListener { task2 ->
                                                if (task2.isSuccessful) {
                                                    // Update password in database
                                                    databaseReference!!
                                                        .child(auth!!.currentUser!!.uid)
                                                        .child("password")
                                                        .setValue(txtNewPassword.text.toString())
                                                        .addOnSuccessListener {
                                                            // Password updated successfully
                                                            MaterialAlertDialogBuilder(requireContext())
                                                                .setTitle("Success")
                                                                .setMessage("Password updated successfully")
                                                                .setPositiveButton("OK") { dialog, which ->
                                                                    btnUpdate.isEnabled = true
                                                                    btnBack.isEnabled = true
                                                                    findNavController().popBackStack()
                                                                }
                                                                .show()
                                                        }
                                                        .addOnFailureListener {
                                                            // Password update failed
                                                            MaterialAlertDialogBuilder(requireContext())
                                                                .setTitle("Error")
                                                                .setMessage("Password update failed")
                                                                .setPositiveButton("OK") { dialog, which ->
                                                                    dialog.dismiss()
                                                                    btnUpdate.isEnabled = true
                                                                    btnBack.isEnabled = true
                                                                }
                                                                .show()
                                                        }
                                                } else {
                                                    // Password update failed
                                                    MaterialAlertDialogBuilder(requireContext())
                                                        .setTitle("Error")
                                                        .setMessage("Password update failed")
                                                        .setPositiveButton("OK") { dialog, which ->
                                                            dialog.dismiss()
                                                            btnUpdate.isEnabled = true
                                                            btnBack.isEnabled = true
                                                        }
                                                        .show()
                                                }
                                            }
                                    } else {
                                        // Current password is incorrect
                                        MaterialAlertDialogBuilder(requireContext())
                                            .setTitle("Error")
                                            .setMessage("Current password is incorrect")
                                            .setPositiveButton("OK") { dialog, which ->
                                                dialog.dismiss()
                                                btnUpdate.isEnabled = true
                                                btnBack.isEnabled = true
                                            }
                                            .show()
                                    }
                                }
                        }
                    }
            } else {
                // Passwords do not match
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("Passwords do not match")
                    .setPositiveButton("OK"){ dialog, which ->
                        dialog.dismiss()
                        btnUpdate.isEnabled = true
                        btnBack.isEnabled = true
                    }
                    .show()
            }
        }

        return view
    }

}