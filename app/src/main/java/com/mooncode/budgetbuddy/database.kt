package com.mooncode.budgetbuddy

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.text.SimpleDateFormat

/**
 * In this file, we define the database structure and the functions to interact with it.
 */
// Database structure
var databaseReference: DatabaseReference? = null
// Database reference to the users
var database: FirebaseDatabase? = null
// Authentication reference
var auth: FirebaseAuth? = null


// Function to add a user to the database
@IgnoreExtraProperties
data class user(
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val birthdayMillis: Long? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    val money: Double? = 0.0): Serializable {

    // Convert the user to a map
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "firstName" to firstName,
            "middleName" to middleName,
            "lastName" to lastName,
            "birthdayMillis" to birthdayMillis,
            "address" to address,
            "phoneNumber" to phoneNumber,
            "email" to email,
            "username" to username,
            "password" to password,
            "money" to money
        )
    }

    // Convert the user to a string
    @Exclude
    override fun toString(): String {
        return "$firstName $middleName $lastName $birthdayMillis $address $phoneNumber $email $username $password $money"
    }
}

// Function to get the consecutive dates from a list of dates in milliseconds
// (ie. [January 1, January 2, January 3, January 5, January 6] -> [[January 1, January 2, January 3], [January 5, January 6]])
// Note: for the example above, the dates are in milliseconds (ie. 1577836800000, 1577923200000, 1578009600000, 1578268800000, 1578355200000)
private fun getConsecutiveNumbers(srcList: List<Long>): List<List<Long>> {
    return srcList.fold(mutableListOf<MutableList<Long>>()) { acc, i ->
        if (acc.isEmpty() || acc.last().last() + 86400000 != i) {
            acc.add(mutableListOf(i))
        } else acc.last().add(i)
        acc
    }
}

// Function to convert a list of dates in milliseconds to a string
// (ie. [January 1, January 2, January 3, January 5, January 6] -> "January 1 - 3, January 5 - 6")
fun dateGroup(lst:  List<Long>): String {
    var datesString = ""
    // get the consecutive dates
    getConsecutiveNumbers(lst).forEach {
        // check if there are more than one date
        if (it.size > 1) {
            // check if the dates are in the same month
            if (SimpleDateFormat("MMMM").format(it[0]) == SimpleDateFormat("MMMM").format(it[it.size - 1]))
                datesString += SimpleDateFormat("MMMM d").format(it[0]) + " - " + SimpleDateFormat("d, yyyy").format(it[it.size - 1]) + "\n"
            // check if the dates are in the same year
            else if (SimpleDateFormat("yyyy").format(it[0]) == SimpleDateFormat("yyyy").format(it[it.size - 1]))
                datesString += SimpleDateFormat("MMM d").format(it[0]) + " - " + SimpleDateFormat("MMM d, yyyy").format(it[it.size - 1]) + "\n"
            // otherwise, the dates are in different years
            else
                datesString += SimpleDateFormat("MMM d, yyyy").format(it[0]) + " - " + SimpleDateFormat("MMM d, yyyy").format(it[it.size - 1]) + "\n"
            // check if there is only one date
        } else {
            datesString += SimpleDateFormat("MMMM d, yyyy").format(it[0]) + "\n"
        }

    }

    return datesString.trim()
}


