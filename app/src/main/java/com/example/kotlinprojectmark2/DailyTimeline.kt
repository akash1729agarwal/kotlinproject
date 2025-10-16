package com.example.kotlinprojectmark2

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kotlinprojectmark2.ui.theme.Kotlinprojectmark2Theme
import java.text.SimpleDateFormat
import java.util.*

enum class EntryType {
    CHECKPOINT,
    TASK
}

data class Entry(
    val id: Int,
    val name: String,
    val type: EntryType
)

@SuppressLint("SimpleDateFormat")
@Composable
fun DailyTimeline(initialEntries: List<Entry>) {
    var entries by remember { mutableStateOf(initialEntries.toMutableList()) }
    var showDialog by remember { mutableStateOf(false) }
    var newEntryType by remember { mutableStateOf(EntryType.CHECKPOINT) }
    var newEntryName by remember { mutableStateOf("") }

    val checkpoints = entries.filter { it.type == EntryType.CHECKPOINT }
    val tasks = entries.filter { it.type == EntryType.TASK }
    val dateFormat = remember { SimpleDateFormat("hh:mm:ss a") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        newEntryType = EntryType.CHECKPOINT
                        showDialog = true
                    }
                ) {
                    Text("Add Checkpoint")
                }

                Button(
                    onClick = {
                        newEntryType = EntryType.TASK
                        showDialog = true
                    }
                ) {
                    Text("Add Task")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Checkpoints",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
            items(checkpoints) { entry -> CheckpointCard(entry, dateFormat) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    text = "Tasks / Activities",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
            items(tasks) { entry -> TaskCard(entry, dateFormat) }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ----- Dialog for adding new entry -----
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add ${if (newEntryType == EntryType.CHECKPOINT) "Checkpoint" else "Task"}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newEntryName,
                        onValueChange = { newEntryName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newEntryName.isNotBlank()) {
                            entries.add(
                                Entry(
                                    id = entries.size + 1,
                                    name = newEntryName.trim(),
                                    type = newEntryType
                                )
                            )
                            newEntryName = ""
                            showDialog = false
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newEntryName = ""
                        showDialog = false
                    }
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CheckpointCard(entry: Entry, dateFormat: SimpleDateFormat) {
    var isChecked by remember { mutableStateOf(false) }
    var checkedTime by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.name, fontWeight = FontWeight.Bold)
                if (checkedTime.isNotEmpty()) {
                    Text("Checked at: $checkedTime", style = MaterialTheme.typography.bodySmall)
                }
            }

            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    isChecked = checked
                    if (checked) {
                        checkedTime = dateFormat.format(Date())
                    } else {
                        checkedTime = ""
                    }
                }
            )
        }
    }
}

@Composable
fun TaskCard(entry: Entry, dateFormat: SimpleDateFormat) {
    var startTime by remember { mutableStateOf<Date?>(null) }
    var endTime by remember { mutableStateOf<Date?>(null) }
    var duration by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(entry.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        startTime = Date()
                        endTime = null
                        duration = ""
                    },
                    enabled = startTime == null
                ) {
                    Text("Start")
                }

                Button(
                    onClick = {
                        if (startTime != null) {
                            endTime = Date()
                            val elapsedMillis = endTime!!.time - startTime!!.time
                            val seconds = elapsedMillis / 1000 % 60
                            val minutes = elapsedMillis / (1000 * 60) % 60
                            val hours = elapsedMillis / (1000 * 60 * 60)
                            duration = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        }
                    },
                    enabled = startTime != null && endTime == null
                ) {
                    Text("Stop")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (startTime != null)
                Text("Start: ${dateFormat.format(startTime!!)}", style = MaterialTheme.typography.bodySmall)
            if (endTime != null)
                Text("End: ${dateFormat.format(endTime!!)}", style = MaterialTheme.typography.bodySmall)
            if (duration.isNotEmpty())
                Text("Elapsed: $duration", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTimeline() {
    Kotlinprojectmark2Theme {
        DailyTimeline(
            initialEntries = listOf(
                Entry(1, "Wake Up", EntryType.CHECKPOINT),
                Entry(2, "Breakfast", EntryType.CHECKPOINT),
                Entry(3, "Start Work", EntryType.CHECKPOINT),
                Entry(4, "Morning Workout", EntryType.TASK),
                Entry(5, "Commute to Work", EntryType.TASK)
            )
        )
    }
}


