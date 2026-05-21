package com.systemleveling.feature.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.database.entity.ItemEntity
import com.systemleveling.core.model.ItemCategory
import com.systemleveling.core.model.ItemRarity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val itemDao: ItemDao
) : ViewModel() {

    private val _items = MutableStateFlow<List<ItemEntity>>(emptyList())
    val items: StateFlow<List<ItemEntity>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            itemDao.getAllItems().collect { dbItems ->
                if (dbItems.isEmpty()) {
                    seedMockItems()
                } else {
                    _items.value = dbItems
                }
            }
        }
    }

    fun useItem(item: ItemEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (item.category == ItemCategory.EQUIPMENT) {
                    // Equipment: just update (no isEquipped field yet — UI handles feedback)
                    itemDao.updateItem(item)
                } else {
                    // Consumable/Material: decrement quantity, delete at 0
                    val updated = item.copy(quantity = item.quantity - 1)
                    if (updated.quantity <= 0) {
                        itemDao.updateItem(updated.copy(quantity = 0))
                    } else {
                        itemDao.updateItem(updated)
                    }
                }
            }
        }
    }

    private suspend fun seedMockItems() {
        val mockData = listOf(
            ItemEntity(id = "I1", name = "Small Potion", description = "Hồi phục một lượng nhỏ thể lực", rarity = ItemRarity.COMMON, category = ItemCategory.CONSUMABLE, quantity = 5, iconId = "🧪"),
            ItemEntity(id = "I2", name = "Speed Boots", description = "Tăng 5 điểm Agility trong 1 giờ", rarity = ItemRarity.UNCOMMON, category = ItemCategory.EQUIPMENT, quantity = 1, iconId = "🥾"),
            ItemEntity(id = "I3", name = "Knowledge Tome", description = "Sách chứa trí tuệ cổ xưa. +10 INT.", rarity = ItemRarity.RARE, category = ItemCategory.CONSUMABLE, quantity = 2, iconId = "📘"),
            ItemEntity(id = "I4", name = "Phoenix Feather", description = "Vật phẩm hồi sinh. Tránh 1 lần Penalty.", rarity = ItemRarity.EPIC, category = ItemCategory.MATERIAL, quantity = 1, iconId = "🪶"),
            ItemEntity(id = "I5", name = "Crown of Wisdom", description = "Vương miện của Bậc Thầy. +50 WIS, +10% EXP", rarity = ItemRarity.LEGENDARY, category = ItemCategory.EQUIPMENT, quantity = 1, iconId = "👑"),
            ItemEntity(id = "I6", name = "Shadow Monarch's Dagger", description = "Vũ khí Thần Thoại. Tăng 100% Sát thương Boss", rarity = ItemRarity.MYTHIC, category = ItemCategory.WEAPON, quantity = 1, iconId = "🗡️"),
            ItemEntity(id = "I7", name = "Basic Scroll", description = "Cuộn giấy phép thuật cơ bản.", rarity = ItemRarity.COMMON, category = ItemCategory.MATERIAL, quantity = 12, iconId = "📜"),
            ItemEntity(id = "I8", name = "Focus Crystal", description = "Tinh thể tập trung. Tăng khả năng Deep Work x2", rarity = ItemRarity.UNCOMMON, category = ItemCategory.CONSUMABLE, quantity = 3, iconId = "🔮")
        )
        itemDao.insertItems(mockData)
    }
}
