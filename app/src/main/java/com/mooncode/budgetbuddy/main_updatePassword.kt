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


        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnUpdate.setOnClickListener {
            btnUpdate.isEnabled = false
            btnBack.isEnabled = false
            if (txtNewPassword.text.toString() == txtConfNewPassword.text.toString()) {
                databaseReference!!
                    .child(auth!!.currentUser!!.uid)
                    .child("password")
                    .get()
                    .addOnSuccessListener {
                        if (it.value.toString() != txtCurrentPassword.text.toString()) {
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
                            var credential = EmailAuthProvider.getCredential(
                                auth!!.currentUser!!.email.toString(),
                                txtCurrentPassword.text.toString())


                            auth!!.currentUser!!.reauthenticate(credential)
                                .addOnCompleteListener {task ->
                                    if (task.isSuccessful){
                                        auth!!.currentUser!!.updatePassword(
                                            txtNewPassword.text.toString())
                                            .addOnCompleteListener { task2 ->
                                                if (task2.isSuccessful) {
                                                    databaseReference!!
                                                        .child(auth!!.currentUser!!.uid)
                                                        .child("password")
                                                        .setValue(txtNewPassword.text.toString())
                                                        .addOnSuccessListener {
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