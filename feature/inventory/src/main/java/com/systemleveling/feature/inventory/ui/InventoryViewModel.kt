package com.systemleveling.feature.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.database.entity.ItemEntity
import com.systemleveling.core.model.ItemCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class InventoryTab { ACTIVE, STORED }

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val itemDao: ItemDao
) : ViewModel() {

    private val _activeTab = MutableStateFlow(InventoryTab.ACTIVE)
    val activeTab: StateFlow<InventoryTab> = _activeTab.asStateFlow()
    private val _selectedCategory = MutableStateFlow<ItemCategory?>(null)
    val selectedCategory: StateFlow<ItemCategory?> = _selectedCategory.asStateFlow()

    private val _activeItems = itemDao.getActiveItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _storedItems = itemDao.getStoredItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedItems: StateFlow<List<ItemEntity>> = combine(
        _activeTab, _selectedCategory, _activeItems, _storedItems
    ) { tab, category, active, stored ->
        val base = if (tab == InventoryTab.ACTIVE) active else stored
        if (category == null) base else base.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = itemDao.getActiveItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val storedCount: StateFlow<Int> = itemDao.getStoredItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setTab(tab: InventoryTab) { _activeTab.value = tab }
    fun setCategory(category: ItemCategory?) { _selectedCategory.value = category }

    fun useItem(item: ItemEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when {
                    item.category == ItemCategory.EQUIPMENT -> itemDao.updateItem(item)
                    item.quantity > 1 -> itemDao.updateItem(item.copy(quantity = item.quantity - 1))
                    else -> itemDao.deleteItem(item.id)
                }
            }
        }
    }

    fun toggleStore(item: ItemEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemDao.updateItem(item.copy(isStored = !item.isStored))
            }
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                itemDao.deleteItem(item.id)
            }
        }
    }

}
