package com.systemleveling.feature.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.engine.LootTable
import com.systemleveling.core.model.ItemCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CompendiumEntry(
    val template: LootTable.ItemTemplate,
    val discovered: Boolean,
    val ownedCount: Int
)

@HiltViewModel
class ItemCompendiumViewModel @Inject constructor(
    private val itemDao: ItemDao
) : ViewModel() {

    val selectedCategory = MutableStateFlow<ItemCategory?>(null)
    val searchQuery = MutableStateFlow("")

    private val allItems = itemDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entries: StateFlow<List<CompendiumEntry>> = combine(
        allItems, selectedCategory, searchQuery
    ) { ownedItems, category, query ->
        val ownedByName = ownedItems.groupBy { it.name }
        LootTable.allTemplates
            .filter { category == null || it.category == category }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .map { tmpl ->
                val owned = ownedByName[tmpl.name]
                CompendiumEntry(
                    template = tmpl,
                    discovered = owned != null,
                    ownedCount = owned?.sumOf { it.quantity } ?: 0
                )
            }
            .sortedWith(compareByDescending<CompendiumEntry> { it.discovered }
                .thenBy { it.template.rarity.ordinal })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val discoveredCount: StateFlow<Int> = allItems
        .map { items ->
            val ownedNames = items.map { it.name }.toSet()
            LootTable.allTemplates.count { it.name in ownedNames }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setCategory(cat: ItemCategory?) { selectedCategory.value = cat }
    fun setSearch(q: String) { searchQuery.value = q }
}
