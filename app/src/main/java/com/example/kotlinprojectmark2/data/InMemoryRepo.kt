package com.example.kotlinprojectmark2.repo

import androidx.compose.runtime.mutableStateListOf
import com.example.kotlinprojectmark2.model.Entry
import com.example.kotlinprojectmark2.model.EntryType

object InMemoryRepo {
    private var nextId = 1
    private val _items = mutableStateListOf<Entry>()
    val items: List<Entry> get() = _items

    fun seed(sample: List<Entry>) {
        _items.clear()
        _items.addAll(sample.map { it.copy(id = nextId++) })
    }

    fun add(name: String, type: EntryType): Entry {
        val e = Entry(id = nextId++, name = name, type = type)
        _items.add(e)
        return e
    }

    fun update(entry: Entry) {
        val idx = _items.indexOfFirst { it.id == entry.id }
        if (idx >= 0) _items[idx] = entry.copy() // ensures notes and state changes persist
    }

    fun delete(entryId: Int) {
        _items.removeAll { it.id == entryId }
    }

    fun getById(id: Int): Entry? = _items.find { it.id == id }
}
