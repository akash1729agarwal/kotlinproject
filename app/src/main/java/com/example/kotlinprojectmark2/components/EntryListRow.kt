package com.example.kotlinprojectmark2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinprojectmark2.model.*
import com.example.kotlinprojectmark2.repo.InMemoryRepo
import com.example.kotlinprojectmark2.util.prettyElapsed
import java.time.Instant
import kotlin.math.max
import kotlinx.coroutines.delay

@Composable
fun EntryListRow(entry: Entry, onClick: () -> Unit) {
    // We intentionally read from repo on every tick so changes elsewhere reflect here.
    var repoEntry by remember { mutableStateOf(InMemoryRepo.getById(entry.id) ?: entry) }
    var tick by remember { mutableStateOf(0) }

    // Single ticker coroutine per row: only increments visually; repo is authoritative.
    LaunchedEffect(key1 = repoEntry.id) {
        while (true) {
            val current = InMemoryRepo.getById(repoEntry.id)
            if (current == null) break
            // If running, tick every second to update the visible stopwatch.
            if (current.runningSince != null) {
                delay(1000)
                tick++ // triggers recomposition and re-reads repo below
                repoEntry = InMemoryRepo.getById(repoEntry.id) ?: repoEntry
            } else {
                // Not running: still keep a small delay to avoid tight loop.
                delay(500)
                // update snapshot so that UI reflects repo changes (like pause/stop)
                repoEntry = InMemoryRepo.getById(repoEntry.id) ?: repoEntry
            }
        }
    }

    // Re-read repo snapshot each composition to reflect external changes immediately.
    repoEntry = InMemoryRepo.getById(entry.id) ?: repoEntry
    val e = repoEntry

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = e.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // For checkpoints show checkbox inline
                if (e.type == EntryType.CHECKPOINT) {
                    var checked by remember { mutableStateOf(e.checkpointCheckedAt != null) }
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { newChecked ->
                            checked = newChecked
                            e.checkpointCheckedAt = if (newChecked) Instant.now() else null
                            InMemoryRepo.update(e)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tasks: stopwatch + buttons
            if (e.type == EntryType.TASK) {
                // compute liveElapsed from authoritative repo state
                val liveElapsed = e.elapsedMillis + (e.runningSince?.let {
                    Instant.now().toEpochMilli() - it.toEpochMilli()
                } ?: 0L)

                // visual stopwatch string (updates when tick changes)
                Text("Elapsed: ${prettyElapsed(liveElapsed)}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(6.dp))

                // Button enable/disable logic derived from repo state:
                val isRunning = e.runningSince != null
                val isStopped = e.stopped
                val hasRecorded = e.elapsedMillis > 0L || isRunning

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Start: enabled only when not running and not stopped
                    Button(
                        onClick = {
                            if (!isRunning && !isStopped) {
                                e.runningSince = Instant.now()
                                InMemoryRepo.update(e)
                                // repoEntry will update on next tick or via update
                            }
                        },
                        enabled = (!isRunning && !isStopped)
                    ) { Text("Start") }

                    // Pause: enabled only when running
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

                    // Stop: enabled when running OR paused (i.e., hasRecorded && not stopped)
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
            }
}}