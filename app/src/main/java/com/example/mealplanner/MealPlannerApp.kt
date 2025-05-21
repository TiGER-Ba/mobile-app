package com.example.mealplanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MealPlannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}