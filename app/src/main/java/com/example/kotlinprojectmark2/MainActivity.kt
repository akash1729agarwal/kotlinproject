package com.example.kotlinprojectmark2



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.kotlinprojectmark2.ui.theme.Kotlinprojectmark2Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        /*setContent {
            Kotlinprojectmark2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    /*Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )*/
                    DailyTimeline(sampleEntries)
                }
            }
        }*/
        setContent {
            DailyTimeline(
                listOf(
                    Entry(1, "Wake Up", EntryType.CHECKPOINT),
                    Entry(2, "Breakfast", EntryType.CHECKPOINT),
                    Entry(3, "Start Work", EntryType.CHECKPOINT),
                    Entry(4, "Morning Workout", EntryType.TASK),
                    Entry(5, "Commute to Work", EntryType.TASK)
                )
            )
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Kotlinprojectmark2Theme {
        Greeting("Android")
    }
}