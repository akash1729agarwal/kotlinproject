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
    var entry by remember { mutableStateOf(InMemoryRepo.getById(entryId)) }

    if (entry == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    var editing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(entry!!.name) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var tickTrigger by remember { mutableStateOf(0) }
    var notesText by remember { mutableStateOf(entry!!.notes) }

    LaunchedEffect(key1 = entry?.runningSince) {
        while (entry?.runningSince != null) {
            delay(1000)
            tickTrigger++
            entry = InMemoryRepo.getById(entryId)
            if (entry == null) break
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Entry Details") }) }) { padding ->
        entry = InMemoryRepo.getById(entryId)
        entry?.let { e ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (editing) {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (editText.isNotBlank()) {
                                e.name = editText.trim()
                                InMemoryRepo.update(e)
                                editing = false
                            }
                        }) { Text("Save") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            editText = e.name
                            editing = false
                        }) { Text("Cancel") }
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(e.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Created: ${formatInstant(e.createdAt)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { editing = true }) { Text("Edit") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
                    }
                }

                if (e.type == EntryType.CHECKPOINT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        var checked by remember { mutableStateOf(e.checkpointCheckedAt != null) }
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { newChecked ->
                                checked = newChecked
                                e.checkpointCheckedAt = if (newChecked) Instant.now() else null
                                InMemoryRepo.update(e)
                            }
                        )
                        Text(
                            if (e.checkpointCheckedAt != null)
                                "Checked at: ${formatInstant(e.checkpointCheckedAt)}"
                            else
                                "Not checked",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    val runningSince = e.runningSince
                    val liveElapsed = e.elapsedMillis +
                            (runningSince?.let {
                                Instant.now().toEpochMilli() - it.toEpochMilli()
                            } ?: 0L)
                    Text("Elapsed: ${prettyElapsed(liveElapsed)}")

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (e.runningSince == null && !e.stopped) {
                                    e.runningSince = Instant.now()
                                    InMemoryRepo.update(e)
                                }
                            },
                            enabled = (e.runningSince == null && !e.stopped)
                        ) { Text("Start") }

                        if (e.runningSince != null) {
                            Button(onClick = {
                                val now = Instant.now()
                                val delta = now.toEpochMilli() - (e.runningSince?.toEpochMilli() ?: now.toEpochMilli())
                                e.elapsedMillis += max(0L, delta)
                                e.runningSince = null
                                InMemoryRepo.update(e)
                            }) { Text("Pause") }
                        } else {
                            Button(
                                onClick = {
                                    if (!e.stopped) {
                                        e.runningSince = Instant.now()
                                        InMemoryRepo.update(e)
                                    }
                                },
                                enabled = (!e.stopped)
                            ) { Text("Resume") }
                        }

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
                            enabled = (!e.stopped)
                        ) { Text("Stop") }
                    }
                }

                // 🆕 Notes field
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

                Spacer(Modifier.height(12.dp))
                Button(onClick = { onNavigateBack() }) { Text("Back") }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this entry?") },
            confirmButton = {
                TextButton(onClick = {
                    InMemoryRepo.delete(entryId)
                    showDeleteConfirm = false
                    onNavigateBack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
