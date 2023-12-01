package com.mooncode.budgetbuddy

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val w = window
        // Hide the status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        // Request camera permission once on app start
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            1
        )

        // Check if network is available, this app will not run without it
        if (!isNetworkAvailable(this)){
            // Show dialog and exit app
            MaterialAlertDialogBuilder(this)
                .setMessage("Network not detected!\nThis app will not run without it!")
                .setPositiveButton("Leave") { _, _ -> finishAndRemoveTask() }
                .setCancelable(false)
                .show()
        // Disable navigation
        } else {
            // Get database instance
            // This is a singleton, so it will only be created once
            database = FirebaseDatabase.getInstance()
            // Get database reference
            // This is where we will store all of our data
            databaseReference = database!!.reference.child("Users")
            // Get auth instance
            // This is where we will check if the user is logged in
            auth = FirebaseAuth.getInstance()

            // Check if user is logged in
            if (auth!!.currentUser != null)
                // User is logged in, navigate to main menu directly
                (supportFragmentManager
                    .findFragmentById(R.id.main_fragment) as NavHostFragment)
                .navController.navigate(R.id.main_menu)
        }
    }

    // Check if network is available
    private fun isNetworkAvailable(context: Context?): Boolean {
        // Check if context is null
        if (context == null) return false
        // Get connectivity manager
        // This is used to check if the device is connected to the internet
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Check if device is running Android 10 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Get network capabilities
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null)
                // Check if device is connected to the internet
                when {
                    // Check if device is connected to the internet via mobile data
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                    // Check if device is connected to the internet via wifi
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                    // Check if device is connected to the internet via ethernet
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
                }
        } else {
            // Get active network info
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            // Check if device is connected to the internet
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) return true
        }
        // Device is not connected to the internet
        return false
    }
}