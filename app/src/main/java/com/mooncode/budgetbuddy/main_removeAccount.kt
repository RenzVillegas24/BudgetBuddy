package com.mooncode.budgetbuddy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider


class main_removeAccount : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_remove_account, container, false)

        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirm)
        val txtPassword = view.findViewById<TextInputEditText>(R.id.txtPassword)
        val txtConfPassword = view.findViewById<TextInputEditText>(R.id.txtConfPassword)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnConfirm.setOnClickListener {
            btnConfirm.isEnabled = false
            btnBack.isEnabled = false
            if (txtPassword.text.toString() == txtConfPassword.text.toString()) {
                databaseReference!!
                    .child(auth!!.currentUser!!.uid)
                    .child("password")
                    .get()
                    .addOnSuccessListener {
                        if (it.value.toString() != txtPassword.text.toString()) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Error")
                                .setMessage("Password is incorrect")
                                .setPositiveButton("OK") { dialog, which ->
                                    dialog.dismiss()
                                    btnConfirm.isEnabled = true
                                    btnBack.isEnabled = true
                                }
                                .show()
                        } else {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Warning")
                                .setMessage("Are you sure you want to delete your account?")
                                .setPositiveButton("Yes") { dialog, which ->
                                    var credential = EmailAuthProvider.getCredential(
                                        auth!!.currentUser!!.email.toString(),
                                        txtPassword.text.toString())


                                    auth!!.currentUser!!.reauthenticate(credential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                databaseReference!!.child(auth!!.currentUser!!.uid)
                                                    .removeValue()
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            auth!!.currentUser!!.delete()
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {
                                                                        MaterialAlertDialogBuilder(
                                                                            requireContext()
                                                                        )
                                                                            .setTitle("Success")
                                                                            .setMessage("Account deleted successfully")
                                                                            .setPositiveButton("OK") { dialog, which ->
                                                                                dialog.dismiss()
                                                                                btnConfirm.isEnabled =
                                                                                    true
                                                                                btnBack.isEnabled =
                                                                                    true
                                                                                findNavController().navigate(
                                                                                    R.id.action_main_removeAccount_to_main_login
                                                                                )
                                                                            }
                                                                            .show()
                                                                    }
                                                                }
                                                        }
                                                    }
                                                    .addOnFailureListener{
                                                        MaterialAlertDialogBuilder(requireContext())
                                                            .setTitle("Error")
                                                            .setMessage("Error happened while deleting account")
                                                            .setPositiveButton("OK") { dialog, which ->
                                                                dialog.dismiss()
                                                                btnConfirm.isEnabled = true
                                                                btnBack.isEnabled = true
                                                            }
                                                            .show()
                                                    }
                                            } else {
                                                MaterialAlertDialogBuilder(requireContext())
                                                    .setTitle("Error")
                                                    .setMessage("Error happened while deleting account")
                                                    .setPositiveButton("OK") { dialog, which ->
                                                        dialog.dismiss()
                                                        btnConfirm.isEnabled = true
                                                        btnBack.isEnabled = true
                                                    }
                                                    .show()

                                            }
                                        }

                                }
                                .setNegativeButton("No") { dialog, which ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    }
                    .addOnFailureListener{
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error")
                            .setMessage("Password is incorrect")
                            .setPositiveButton("OK") { dialog, which ->
                                dialog.dismiss()
                                btnConfirm.isEnabled = true
                                btnBack.isEnabled = true
                            }
                            .show()
                    }
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("Password does not match")
                    .setPositiveButton("OK") { dialog, which ->
                        dialog.dismiss()
                        btnConfirm.isEnabled = true
                        btnBack.isEnabled = true
                    }
                    .show()
            }
        }



        return view
    }

}