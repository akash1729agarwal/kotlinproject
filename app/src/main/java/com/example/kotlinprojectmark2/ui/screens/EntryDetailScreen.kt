@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.kotlinprojectmark2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinprojectmark2.model.*
import com.example.kotlinprojectmark2.repo.InMemoryRepo
import com.example.kotlinprojectmark2.util.*
import kotlinx.coroutines.delay
import java.time.Instant
import kotlin.math.max


@Composable
fun EntryDetailScreen(entryId: Int, onNavigateBack: () -> Unit) {
    // keep a local snapshot, but authoritative state is pulled from InMemoryRepo
    var repoEntry by remember { mutableStateOf(InMemoryRepo.getById(entryId)) }
    if (repoEntry == null) {
        // if entry removed, go back gracefully
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    // visual ticker to update the UI while running
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(key1 = entryId) {
        while (true) {
            val current = InMemoryRepo.getById(entryId)
            if (current == null) break
            // if running, update every second for seamless UI
            if (current.runningSince != null) {
                delay(1000)
                tick++
                repoEntry = InMemoryRepo.getById(entryId)
            } else {
                // even when not running, occasionally refresh so UI reflects repo updates
                delay(500)
                repoEntry = InMemoryRepo.getById(entryId)
            }
        }
    }

    // always read latest authoritative state before rendering
    repoEntry = InMemoryRepo.getById(entryId) ?: repoEntry
    val e: Entry = repoEntry!!

    // ui states for editing notes/name are local but persisted to repo on change
    var editing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(e.name) }
    var notesText by remember { mutableStateOf(e.notes) } // if Entry has notes field
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Entry Details") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title + createdAt
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (editing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (editText.isNotBlank()) {
                            e.name = editText.trim()
                            InMemoryRepo.update(e)
                            editing = false
                        }
                    }) { Text("Save") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        editText = e.name
                        editing = false
                    }) { Text("Cancel") }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(e.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Created: ${formatInstant(e.createdAt)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { editing = true }) { Text("Edit") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
                }
            }

            // Controls & stopwatch
            if (e.type == EntryType.CHECKPOINT) {
                var checked by remember { mutableStateOf(e.checkpointCheckedAt != null) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = { newChecked ->
                        checked = newChecked
                        e.checkpointCheckedAt = if (newChecked) Instant.now() else null
                        InMemoryRepo.update(e)
                    })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (e.checkpointCheckedAt != null)
                            "Checked at: ${formatInstant(e.checkpointCheckedAt)}"
                        else
                            "Not checked"
                    )
                }
            } else {
                // Task with stopwatch and controls
                val isRunning = e.runningSince != null
                val isStopped = e.stopped
                val hasRecorded = e.elapsedMillis > 0L || isRunning
                val liveElapsed = e.elapsedMillis + (e.runningSince?.let {
                    Instant.now().toEpochMilli() - it.toEpochMilli()
                } ?: 0L)

                Text("Elapsed: ${prettyElapsed(liveElapsed)}", style = MaterialTheme.typography.titleMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Start
                    Button(
                        onClick = {
                            if (!isRunning && !isStopped) {
                                e.runningSince = Instant.now()
                                InMemoryRepo.update(e)
                            }
                        },
                        enabled = (!isRunning && !isStopped)
                    ) { Text("Start") }

                    // Pause
                    Button(
                        onClick = {
                            if (isRunning) {
                                val now = Instant.now()
                                val delta = now.toEpochMilli() - (e.runningSince?.toEpochMilli() ?: now.toEpochMilli())
                                e.elapsedMillis += max(0L, delta)
                                e.runningSince = null
                                InMemoryRepo.update(e)
                            }
                        },
                        enabled = isRunning
                    ) { Text("Pause") }

                    // Stop
                    Button(
                        onClick = {
                            if (e.runningSince != null) {
                                val now = Instant.now()
                                val delta = now.toEpochMilli() - (e.runningSince?.toEpochMilli() ?: now.toEpochMilli())
                                e.elapsedMillis += max(0L, delta)
                                e.runningSince = null
                            }
                            e.stopped = true
                            InMemoryRepo.update(e)
                        },
                        enabled = (hasRecorded && !isStopped)
                    ) { Text("Stop") }
                }
            }

            Divider()

            // Notes: update repo on change immediately
            Text("Notes:", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                    e.notes = it
                    InMemoryRepo.update(e)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onNavigateBack() }) { Text("Back") }
                TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this entry? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    InMemoryRepo.delete(entryId)
                    showDeleteConfirm = false
                    onNavigateBack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } })
    }
}
