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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val activeTab = MutableStateFlow(InventoryTab.ACTIVE)
    val selectedCategory = MutableStateFlow<ItemCategory?>(null)

    private val _activeItems = itemDao.getActiveItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _storedItems = itemDao.getStoredItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedItems: StateFlow<List<ItemEntity>> = combine(
        activeTab, selectedCategory, _activeItems, _storedItems
    ) { tab, category, active, stored ->
        val base = if (tab == InventoryTab.ACTIVE) active else stored
        if (category == null) base else base.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = itemDao.getActiveItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val storedCount: StateFlow<Int> = itemDao.getStoredItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSeeded = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            itemDao.getAllItems().collect { dbItems ->
                if (dbItems.isEmpty() && !_isSeeded.value) {
                    _isSeeded.value = true
                    seedMockItems()
                }
            }
        }
    }

    fun setTab(tab: InventoryTab) { activeTab.value = tab }
    fun setCategory(category: ItemCategory?) { selectedCategory.value = category }

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

    private suspend fun seedMockItems() {
        val mockData = listOf(
            ItemEntity(id = "I1", name = "Small Potion", description = "Hồi phục một lượng nhỏ thể lực", loreDescription = "Bình thuốc nhỏ màu đỏ, tỏa ra mùi thảo dược ngọt ngào.", rarity = ItemRarity.COMMON, category = ItemCategory.CONSUMABLE, quantity = 5, iconId = "🧪"),
            ItemEntity(id = "I2", name = "Speed Boots", description = "Tăng 5 điểm Agility trong 1 giờ", loreDescription = "Đôi giày enchanted từ thợ rèn huyền thoại vùng Đông Bắc.", rarity = ItemRarity.UNCOMMON, category = ItemCategory.EQUIPMENT, quantity = 1, iconId = "🥾"),
            ItemEntity(id = "I3", name = "Knowledge Tome", description = "Sách chứa trí tuệ cổ xưa. +10 INT.", loreDescription = "Trang sách vẫn còn ấm dù nằm trong dungeon hàng thế kỷ.", rarity = ItemRarity.RARE, category = ItemCategory.CONSUMABLE, quantity = 2, iconId = "📘"),
            ItemEntity(id = "I4", name = "Phoenix Feather", description = "Vật phẩm hồi sinh. Tránh 1 lần Penalty.", loreDescription = "Chiếc lông rực cháy với ngọn lửa bất diệt — biểu tượng của sự tái sinh.", rarity = ItemRarity.EPIC, category = ItemCategory.MATERIAL, quantity = 1, iconId = "🪶"),
            ItemEntity(id = "I5", name = "Crown of Wisdom", description = "Vương miện của Bậc Thầy. +50 WIS, +10% EXP", loreDescription = "Vương miện của Đại Pháp Sư thời kỳ hoàng kim, được đúc từ kim cương ma thuật.", rarity = ItemRarity.LEGENDARY, category = ItemCategory.EQUIPMENT, quantity = 1, iconId = "👑"),
            ItemEntity(id = "I6", name = "Shadow Monarch's Dagger", description = "Vũ khí Thần Thoại. Tăng 100% Sát thương Boss", loreDescription = "Dao găm của Shadow Monarch — chỉ những kẻ được chọn mới cảm nhận được sức mạnh từ nó.", rarity = ItemRarity.MYTHIC, category = ItemCategory.WEAPON, quantity = 1, iconId = "🗡️"),
            ItemEntity(id = "I7", name = "Basic Scroll", description = "Cuộn giấy phép thuật cơ bản.", loreDescription = "Một cuộn giấy nhàm chán, nhưng ẩn chứa tiềm năng vô hạn.", rarity = ItemRarity.COMMON, category = ItemCategory.MATERIAL, quantity = 12, iconId = "📜"),
            ItemEntity(id = "I8", name = "Focus Crystal", description = "Tinh thể tập trung. Tăng khả năng Deep Work x2", loreDescription = "Viên pha lê trong suốt phát ra ánh sáng ấm áp khi chạm vào.", rarity = ItemRarity.UNCOMMON, category = ItemCategory.CONSUMABLE, quantity = 3, iconId = "🔮")
        )
        itemDao.insertItems(mockData)
    }
}
