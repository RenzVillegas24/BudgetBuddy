package com.mooncode.budgetbuddy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener



class main_login : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_main_login, container, false)

        var btnLogin = view.findViewById<Button>(R.id.btnLogin)
        var btnRegister = view.findViewById<Button>(R.id.btnRegister)
        var txtUsername = view.findViewById<TextInputEditText>(R.id.txtUsername)
        var txtPassword = view.findViewById<TextInputEditText>(R.id.txtPassword)

        // used for login
        btnLogin.setOnClickListener {
            // start with enabling the buttons
            btnLogin.isEnabled = false
            btnRegister.isEnabled = false

            // set the username
            val username = txtUsername.text.toString()
            // check if the username is the same as the one in the database
            databaseReference!!
                .orderByChild("username")
                .equalTo(username)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.i("", "dataSnapshot value = " + dataSnapshot.value)

                            // check if the username exists
                            if (dataSnapshot.exists()) {
                                // get the email from the database using the username
                                val email = dataSnapshot.children.first().child("email").value.toString()

                                Log.d("", email)

                                // sign in with the email and password
                                auth!!.signInWithEmailAndPassword(email, txtPassword.text.toString())
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("", "signinUserWithEmail:success\nUSERNAME: ${txtUsername.text.toString()}\nPASSWORD: ${txtPassword.text.toString()}")
                                            val user = auth!!.currentUser

                                            MaterialAlertDialogBuilder(requireActivity())
                                                .setMessage("Login Successful!\nWelcome back to Budget Buddy, ${txtUsername.text.toString()}!")
                                                .setPositiveButton("OK") { _, _ -> findNavController().navigate(R.id.action_main_login_to_main_menu)}
                                                .setCancelable(false)
                                                .show()

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("", "signinUserWithEmail:failure\nUSERNAME: ${txtUsername.text.toString()}\nPASSWORD: ${txtPassword.text.toString()}", task.exception)


                                            MaterialAlertDialogBuilder(requireActivity())
                                                .setMessage("Authentication failed.\nPlease check your username and password.")
                                                .setPositiveButton("OK"){    _, _ ->
                                                    btnLogin.isEnabled = true
                                                    btnRegister.isEnabled = true
                                                }
                                                .setCancelable(false)
                                                .show()
                                        }
                                    }

                            } else {
                                // User Not Yet Exists
                                MaterialAlertDialogBuilder(requireActivity())
                                    .setTitle("Error")
                                    .setMessage("Username not exists. Please try other username.")
                                    .setPositiveButton("OK") { _, _ ->
                                        btnLogin.isEnabled = true
                                        btnRegister.isEnabled = true
                                    }
                                    .setCancelable(false)
                                    .show()

                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    }
                )


        }

        // used for register
        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_main_login_to_main_register)
        }

        return view

    }

}