package com.systemleveling.core.model

enum class SkillLevel(val title: String, val maxSp: Int) {
    NOVICE("Nhập Môn", 100),
    APPRENTICE("Sơ Cấp", 300),
    INTERMEDIATE("Trung Sơ Cấp", 600),
    ADVANCED("Trung Cấp", 1000),
    EXPERT("Tiền Cao Cấp", 1800),
    MASTER("Cao Cấp", 3000),
    GRAND_MASTER("Grand Master", 9999)
}
