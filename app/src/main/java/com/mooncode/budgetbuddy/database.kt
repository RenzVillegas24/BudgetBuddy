package com.mooncode.budgetbuddy

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.text.SimpleDateFormat


var databaseReference: DatabaseReference? = null
var database: FirebaseDatabase? = null
var auth: FirebaseAuth? = null



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

    @Exclude
    override fun toString(): String {
        return "$firstName $middleName $lastName $birthdayMillis $address $phoneNumber $email $username $password $money"
    }
}

private fun getConsecutiveNumbers(srcList: List<Long>): List<List<Long>> {
    return srcList.fold(mutableListOf<MutableList<Long>>()) { acc, i ->
        if (acc.isEmpty() || acc.last().last() + 86400000 != i) {
            acc.add(mutableListOf(i))
        } else acc.last().add(i)
        acc
    }
}

fun dateGroup(lst:  List<Long>): String {
    var datesString = ""
    getConsecutiveNumbers(lst).forEach {
        if (it.size > 1) {
            // check if the dates are in the same month
            if (SimpleDateFormat("MMMM").format(it[0]) == SimpleDateFormat("MMMM").format(it[it.size - 1]))
                datesString += SimpleDateFormat("MMMM d").format(it[0]) + " - " + SimpleDateFormat("d, yyyy").format(it[it.size - 1]) + "\n"
            // check if the dates are in the same year
            else if (SimpleDateFormat("yyyy").format(it[0]) == SimpleDateFormat("yyyy").format(it[it.size - 1]))
                datesString += SimpleDateFormat("MMM d").format(it[0]) + " - " + SimpleDateFormat("MMM d, yyyy").format(it[it.size - 1]) + "\n"
            else
                datesString += SimpleDateFormat("MMM d, yyyy").format(it[0]) + " - " + SimpleDateFormat("MMM d, yyyy").format(it[it.size - 1]) + "\n"
        } else {
            datesString += SimpleDateFormat("MMMM d, yyyy").format(it[0]) + "\n"
        }

    }

    return datesString.trim()
}


