package com.systemleveling.core.model

enum class ItemRarity(val title: String) {
    COMMON("Thường"),
    UNCOMMON("Hiếm"),
    RARE("Quý hiếm"),
    EPIC("Sử thi"),
    LEGENDARY("Huyền thoại"),
    MYTHIC("Thần thoại")
}

enum class ItemCategory(val title: String) {
    CONSUMABLE("Tiêu hao"),
    EQUIPMENT("Trang bị"),
    WEAPON("Vũ khí"),
    COLLECTIBLE("Sưu tập"),
    MATERIAL("Nguyên liệu"),
    QUEST("Nhiệm vụ"),
    SPECIAL("Đặc biệt")
}
