package com.systemleveling.core.database.converter

import androidx.room.TypeConverter
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.QuestType
import com.systemleveling.core.model.RecurrenceType
import com.systemleveling.core.model.SkillLevel

class AppTypeConverters {
    @TypeConverter
    fun fromQuestType(value: QuestType): String = value.name

    @TypeConverter
    fun toQuestType(value: String): QuestType = enumValueOf(value)

    @TypeConverter
    fun fromQuestRank(value: QuestRank): String = value.name

    @TypeConverter
    fun toQuestRank(value: String): QuestRank = enumValueOf(value)

    @TypeConverter
    fun fromQuestStatus(status: QuestStatus): String = status.name

    @TypeConverter
    fun toQuestStatus(name: String): QuestStatus = QuestStatus.valueOf(name)

    // --- Skill ---
    @TypeConverter
    fun fromSkillLevel(level: SkillLevel): String = level.name

    @TypeConverter
    fun toSkillLevel(name: String): SkillLevel = SkillLevel.valueOf(name)

    // --- Item ---
    @TypeConverter
    fun fromItemRarity(rarity: com.systemleveling.core.model.ItemRarity): String = rarity.name

    @TypeConverter
    fun toItemRarity(name: String): com.systemleveling.core.model.ItemRarity = com.systemleveling.core.model.ItemRarity.valueOf(name)

    @TypeConverter
    fun fromItemCategory(category: com.systemleveling.core.model.ItemCategory): String = category.name

    @TypeConverter
    fun toItemCategory(name: String): com.systemleveling.core.model.ItemCategory = com.systemleveling.core.model.ItemCategory.valueOf(name)

    // --- Finance ---
    @TypeConverter
    fun fromTransactionType(type: com.systemleveling.core.model.TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(name: String): com.systemleveling.core.model.TransactionType = com.systemleveling.core.model.TransactionType.valueOf(name)

    @TypeConverter
    fun fromFinanceCategory(category: com.systemleveling.core.model.FinanceCategory): String = category.name

    @TypeConverter
    fun toFinanceCategory(name: String): com.systemleveling.core.model.FinanceCategory = com.systemleveling.core.model.FinanceCategory.valueOf(name)

    @TypeConverter
    fun toMood(value: String): com.systemleveling.core.model.Mood {
        return enumValueOf<com.systemleveling.core.model.Mood>(value)
    }

    @TypeConverter
    fun fromMood(mood: com.systemleveling.core.model.Mood): String {
        return mood.name
    }

    // --- RecurrenceType ---
    @TypeConverter
    fun fromRecurrenceType(type: RecurrenceType): String = type.name

    @TypeConverter
    fun toRecurrenceType(name: String): RecurrenceType = RecurrenceType.valueOf(name)
}
