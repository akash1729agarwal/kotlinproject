package com.example.kotlinprojectmark2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kotlinprojectmark2.model.Entry
import com.example.kotlinprojectmark2.model.EntryType
import com.example.kotlinprojectmark2.repo.InMemoryRepo
import com.example.kotlinprojectmark2.navigation.AppNavHost
import com.example.kotlinprojectmark2.ui.theme.Kotlinprojectmark2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // seed sample data
        InMemoryRepo.seed(listOf(
            Entry(0, "Wake Up", EntryType.CHECKPOINT),
            Entry(0, "Breakfast", EntryType.CHECKPOINT),
            Entry(0, "Morning Workout", EntryType.TASK),
            Entry(0, "Commute", EntryType.TASK)
        ))

        setContent {
            Kotlinprojectmark2Theme {
                AppNavHost()
            }
        }
    }
}
