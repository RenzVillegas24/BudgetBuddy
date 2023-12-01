package com.mooncode.budgetbuddy

import android.icu.util.Calendar
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.log

// we used this to use only one fragment for creating new account from the user profile and from the login page
private const val ARG_PARAM1 = "autoLogin"
private const val ARG_PARAM2 = "loggedIn"

class main_register : Fragment() {
    // identifies if the account will be automatically logged in after creation
    private var autoLogin: Boolean? = null
    // identifies if the account is created from the user profile or from the login page
    private var loggedIn: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get the arguments passed from the user profile or from the login page
        arguments?.let {
            autoLogin = it.getBoolean(ARG_PARAM1)
            loggedIn = it.getBoolean(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_main_register, container, false)

        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val txtFirstName = view.findViewById<TextInputEditText>(R.id.txtFirstName)
        val txtMiddleName = view.findViewById<TextInputEditText>(R.id.txtMiddleInitial)
        val txtLastName = view.findViewById<TextInputEditText>(R.id.txtLastName)
        val txtBirthday = view.findViewById<TextInputEditText>(R.id.txtBirthday)
        val txtAddress = view.findViewById<TextInputEditText>(R.id.txtAddress)
        val txtPhoneNumber = view.findViewById<TextInputEditText>(R.id.txtPhoneNumber)
        val txtEmail = view.findViewById<TextInputEditText>(R.id.txtEmail)
        val txtUsername = view.findViewById<TextInputEditText>(R.id.txtUsername)
        val txtPassword = view.findViewById<TextInputEditText>(R.id.txtPassword)
        val txtConfPassword = view.findViewById<TextInputEditText>(R.id.txtConfPassword)
        val cbTermsAndConditions = view.findViewById<CheckBox>(R.id.cbTermsAndConditions)
        val tvTermsAndConditions = view.findViewById<TextView>(R.id.tvTermsAndConditions)
        val tvBtnLowerText = view.findViewById<TextView>(R.id.tvBtnLowerText)

        var selected_birthDay: Long? = null

        // change the text of the button and the text below the button if the account is created from the user profile
        if (loggedIn != null && loggedIn!!) {
            btnLogin.text = "Back"
            tvBtnLowerText.text = "Want to go back to the profile?\nClick here to continue,"
        }

        Log.d("autoLogin", autoLogin.toString() + " " + loggedIn.toString())

        btnRegister.setOnClickListener {
            // disable the buttons to prevent multiple clicks
            btnLogin.isEnabled = false
            btnRegister.isEnabled = false

            // check if all the fields are filled up
            if (txtFirstName.text.isNullOrBlank() ||
                txtMiddleName.text.isNullOrBlank() ||
                txtLastName.text.isNullOrBlank() ||
                selected_birthDay == null ||
                txtAddress.text.isNullOrBlank() ||
                txtPhoneNumber.text.isNullOrBlank() ||
                txtEmail.text.isNullOrBlank() ||
                txtUsername.text.isNullOrBlank() ||
                txtPassword.text.isNullOrBlank() ||
                txtConfPassword.text.isNullOrBlank()) {

                MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Please fill up all the fields")
                    .setPositiveButton("Try Again"){    _, _ ->
                        btnLogin.isEnabled = true
                        btnRegister.isEnabled = true
                    }
                    .setCancelable(false)
                    .show()

                return@setOnClickListener
            } else {
                // check if the password and the confirmation password matches

                if (txtPassword.text.toString() != txtConfPassword.text.toString()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Password and Confirmation Password does not match")
                        .setPositiveButton("Try Again"){    _, _ ->
                            // re-enable the buttons
                            btnLogin.isEnabled = true
                            btnRegister.isEnabled = true
                        }
                        .setCancelable(false)
                        .show()

                    return@setOnClickListener
                }

                // check if the terms and conditions checkbox is checked
                if (!cbTermsAndConditions.isChecked) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Please agree to the Terms and Conditions")
                        .setPositiveButton("Try Again"){    _, _ ->
                            btnLogin.isEnabled = true
                            btnRegister.isEnabled = true
                        }
                        .setCancelable(false)
                        .show()

                    return@setOnClickListener
                }

                // check if the username already exists
                databaseReference!!
                    .orderByChild("username")
                    .equalTo(txtUsername.text.toString())
                    .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    MaterialAlertDialogBuilder(requireContext())
                                        .setMessage("Username already exists. Please try other username.")
                                        .setPositiveButton("Try Again"){    _, _ ->
                                            // re-enable the buttons
                                            btnLogin.isEnabled = true
                                            btnRegister.isEnabled = true
                                        }
                                        .setCancelable(false)
                                        .show()

                                } else {

                                    var registerUser = auth
                                    // if the account is created from the user profile, use the current user
                                    if (autoLogin != null && !autoLogin!!)
                                    {

                                        // createUserWithEmailAndPassword without signing in
                                        // this is to prevent to automatically log in the user, by default it will automatically log in the user
                                        // but we don't want that to happen, instead we will create new instance of firebase app and use that to create the user
                                        // then dispose the instance after creating the user
                                        val signUp = FirebaseApp.initializeApp(
                                            requireContext(),
                                            Firebase.auth.app.options,
                                            "secondary")

                                        registerUser = FirebaseAuth.getInstance(signUp)

                                        Log.d("signup/update", (auth!!.currentUser).toString() +  (registerUser.currentUser).toString())
                                    }

                                    // create the user
                                    registerUser!!.createUserWithEmailAndPassword(txtEmail.text.toString(), txtPassword.text.toString())
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d("", "createUserWithEmail:success\nUSERNAME: ${txtUsername.text}\nPASSWORD: ${txtPassword.text}")

                                                // add the user to the database
                                                databaseReference!!
                                                    .child(registerUser!!.currentUser!!.uid)
                                                    .setValue(user(
                                                        txtFirstName.text.toString(),
                                                        txtMiddleName.text.toString(),
                                                        txtLastName.text.toString(),
                                                        selected_birthDay,
                                                        txtAddress.text.toString(),
                                                        txtPhoneNumber.text.toString(),
                                                        txtEmail.text.toString(),
                                                        txtUsername.text.toString(),
                                                        txtPassword.text.toString()))
                                                    .addOnSuccessListener {
                                                        MaterialAlertDialogBuilder(requireActivity())
                                                            .setTitle("Creation Success")
                                                            .setMessage("Account successfully created!\nWelcome to Budget Buddy, ${txtFirstName.text}!")
                                                            .setPositiveButton("Continue") { _, _ ->
                                                                // if the account is created from the user profile, go back to the profile
                                                                if (autoLogin != null && !autoLogin!!)
                                                                    findNavController().popBackStack()
                                                                // if the account is created from the login page, go to the main menu
                                                                else
                                                                    findNavController().navigate(R.id.action_main_register_to_main_menu)
                                                            }
                                                            .setCancelable(false)
                                                            .show()


                                                        Log.d("signup/update", "success")
                                                    }
                                                    .addOnFailureListener {
                                                        MaterialAlertDialogBuilder(requireActivity())
                                                            .setTitle("Creation Failed")
                                                            .setMessage("Account creation failed! Please try again.")
                                                            .setPositiveButton("Try Again"){    _, _ ->
                                                                btnLogin.isEnabled = true
                                                                btnRegister.isEnabled = true
                                                            }
                                                            .setCancelable(false)
                                                            .show()
                                                        Log.d("signup/update", "failed")


                                                    }


                                                } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w("", "createUserWithEmail:failure\nUSERNAME: ${txtUsername.text}\nPASSWORD: ${txtPassword.text}")
                                                Toast.makeText(
                                                    context,
                                                    "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show()
                                            }


                                            // if the account is created from the user profile, sign out the current user and delete the instance of the firebase app
                                            if (autoLogin != null && !autoLogin!!)
                                            {
                                                Log.d("signup/update", (auth!!.currentUser != null ).toString() +  (Firebase.auth!!.currentUser!!.uid != null).toString())
                                                registerUser.signOut()
                                                FirebaseApp.getInstance("secondary").delete()
                                            }
                                        }

                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
            }
        }

        // go back to the login page or to the user profile
        btnLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        // open the date picker
        txtBirthday.setOnClickListener {
            it as TextInputEditText

            // same as the date picker used for other fragments
            val constraint = CalendarConstraints.Builder()
            val calendar = Calendar.getInstance()

            calendar.add(Calendar.YEAR, -13)
            constraint.setEnd(calendar.timeInMillis)

            if (selected_birthDay != null)
                constraint.setOpenAt(if (it.text.isNullOrBlank()) calendar.timeInMillis else selected_birthDay!!)
            else
                constraint.setOpenAt(calendar.timeInMillis)
            

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

        // make the terms and conditions clickable
        tvTermsAndConditions.makeLinks(
            Pair("Terms and Conditions", View.OnClickListener {
                findNavController().navigate(R.id.action_main_register_to_main_termsAndConditions)
            })
        )

        // Inflate the layout for this fragment
        return view
    }

    // this function is used to make specific parts of the text clickable
    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        // find the index of the text to be clickable
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                // change the color of the text to be clickable
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color = textPaint.linkColor
                    textPaint.isUnderlineText = true
                }
                // when the text is clicked, call the onClick function
                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
            Log.d("startIndexOfLink", this.text.toString())
            if(startIndexOfLink == -1) continue
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }


}