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
        val w = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val data = intent.data.toString()
        val dataArgs = data.replace("BudgetBuddy://", "")
        when (dataArgs) {
            "transfer" -> {}
        }

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            1
        )

        if (!isNetworkAvailable(this)){
            MaterialAlertDialogBuilder(this)
                .setMessage("Network not detected!\nThis app will not run without it!")
                .setPositiveButton("Leave") { _, _ -> finishAndRemoveTask() }
                .setCancelable(false)
                .show()
        } else {
            database = FirebaseDatabase.getInstance()
            databaseReference = database!!.reference.child("Users")
            auth = FirebaseAuth.getInstance()

            if (auth!!.currentUser != null)
                (supportFragmentManager
                    .findFragmentById(R.id.main_fragment) as NavHostFragment)
                .navController.navigate(R.id.main_menu)
        }


    }


    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null)
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
                }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) return true
        }
        return false
    }
}