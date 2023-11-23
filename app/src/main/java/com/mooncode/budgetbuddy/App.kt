package com.mooncode.budgetbuddy

import android.app.Application
import com.google.android.material.color.DynamicColors

class App: Application(){
    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)
        super.onCreate()
    }
}