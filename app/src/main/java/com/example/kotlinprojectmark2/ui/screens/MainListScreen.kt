@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.kotlinprojectmark2.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlinprojectmark2.repo.InMemoryRepo
import com.example.kotlinprojectmark2.model.EntryType
import com.example.kotlinprojectmark2.model.Entry
import com.example.kotlinprojectmark2.ui.EntryListRow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment

@SuppressLint("SimpleDateFormat")
@Composable
fun MainListScreen(onOpenDetail: (Int) -> Unit) {
    // snapshot repo items (the repo is stateful so UI will recompose when it changes)
    var entries by remember { mutableStateOf(InMemoryRepo.items.toList()) }
    entries = InMemoryRepo.items.toList()

    var showDialog by remember { mutableStateOf(false) }
    var newEntryType by remember { mutableStateOf(EntryType.CHECKPOINT) }
    var newEntryName by remember { mutableStateOf("") }

    val checkpoints = entries.filter { it.type == EntryType.CHECKPOINT }
    val tasks = entries.filter { it.type == EntryType.TASK }

    Scaffold(topBar = { TopAppBar(title = { Text("Daily Timeline") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Checkpoints",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            items(checkpoints) { entry -> EntryListRow(entry = entry, onClick = { onOpenDetail(entry.id) }) }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Button(onClick = { newEntryType = EntryType.CHECKPOINT; showDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Checkpoint")
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    "Tasks / Activities",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            items(tasks) { entry -> EntryListRow(entry = entry, onClick = { onOpenDetail(entry.id) }) }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Button(onClick = { newEntryType = EntryType.TASK; showDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Task")
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false },
            title = { Text("Add ${if (newEntryType == EntryType.CHECKPOINT) "Checkpoint" else "Task"}") },
            text = { OutlinedTextField(value = newEntryName, onValueChange = { newEntryName = it }, label = { Text("Name") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (newEntryName.isNotBlank()) {
                        InMemoryRepo.add(newEntryName.trim(), newEntryType)
                        newEntryName = ""
                        showDialog = false
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
