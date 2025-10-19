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

@Composable
fun EntryListRow(entry: Entry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(entry.name, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            when (entry.type) {
                EntryType.CHECKPOINT -> {
                    var checked by remember { mutableStateOf(entry.checkpointCheckedAt != null) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { newChecked ->
                                checked = newChecked
                                entry.checkpointCheckedAt = if (newChecked) Instant.now() else null
                                InMemoryRepo.update(entry)
                            }
                        )
                        Text(
                            if (entry.checkpointCheckedAt != null)
                                "Checked"
                            else
                                "Not checked",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                EntryType.TASK -> {
                    val runningSince = entry.runningSince
                    val liveElapsed = entry.elapsedMillis +
                            (runningSince?.let {
                                Instant.now().toEpochMilli() - it.toEpochMilli()
                            } ?: 0L)

                    Text("Elapsed: ${prettyElapsed(liveElapsed)}")

                    Spacer(Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (entry.runningSince == null && !entry.stopped) {
                                    entry.runningSince = Instant.now()
                                    InMemoryRepo.update(entry)
                                }
                            },
                            enabled = (entry.runningSince == null && !entry.stopped)
                        ) { Text("Start") }

                        if (entry.runningSince != null) {
                            Button(onClick = {
                                val now = Instant.now()
                                val delta = now.toEpochMilli() - (entry.runningSince?.toEpochMilli() ?: now.toEpochMilli())
                                entry.elapsedMillis += max(0L, delta)
                                entry.runningSince = null
                                InMemoryRepo.update(entry)
                            }) { Text("Pause") }
                        } else {
                            Button(
                                onClick = {
                                    if (!entry.stopped) {
                                        entry.runningSince = Instant.now()
                                        InMemoryRepo.update(entry)
                                    }
                                },
                                enabled = (!entry.stopped)
                            ) { Text("Resume") }
                        }

                        Button(
                            onClick = {
                                if (entry.runningSince != null) {
                                    val now = Instant.now()
                                    val delta = now.toEpochMilli() - (entry.runningSince?.toEpochMilli() ?: now.toEpochMilli())
                                    entry.elapsedMillis += max(0L, delta)
                                    entry.runningSince = null
                                }
                                entry.stopped = true
                                InMemoryRepo.update(entry)
                            },
                            enabled = (!entry.stopped)
                        ) { Text("Stop") }
                    }
                }
            }
        }
    }
}
