package com.example.kotlinprojectmark2.model

import java.time.Instant

enum class EntryType { CHECKPOINT, TASK }

data class Entry(
    val id: Int,
    var name: String,
    val type: EntryType,
    val createdAt: Instant = Instant.now(),

    // runtime state for checkpoint
    var checkpointCheckedAt: Instant? = null,

    // runtime / persisted state for task timing
    var elapsedMillis: Long = 0L,
    var runningSince: Instant? = null,
    var stopped: Boolean = false,

    // new field for notes
    var notes: String = ""
)
