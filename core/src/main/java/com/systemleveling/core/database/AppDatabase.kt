package com.systemleveling.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.systemleveling.core.database.converter.AppTypeConverters
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.database.dao.TitleDao
import com.systemleveling.core.database.dao.FinanceDao
import com.systemleveling.core.database.dao.CourseDao
import com.systemleveling.core.database.dao.JournalDao
import com.systemleveling.core.database.dao.DailySummaryDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.database.entity.SkillEntity
import com.systemleveling.core.database.entity.ItemEntity
import com.systemleveling.core.database.entity.TitleEntity
import com.systemleveling.core.database.entity.TransactionEntity
import com.systemleveling.core.database.entity.CourseEntity
import com.systemleveling.core.database.entity.JournalEntity
import com.systemleveling.core.database.entity.DailySummaryEntity
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity

@Database(entities = [UserEntity::class, StatEntity::class, QuestEntity::class, SkillEntity::class, ItemEntity::class, TitleEntity::class, TransactionEntity::class, CourseEntity::class, JournalEntity::class, DailySummaryEntity::class], version = 6, exportSchema = false)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questDao(): QuestDao
    abstract fun skillDao(): SkillDao
    abstract fun itemDao(): ItemDao
    abstract fun titleDao(): TitleDao
    abstract fun financeDao(): FinanceDao
    abstract fun courseDao(): CourseDao
    abstract fun journalDao(): JournalDao
    abstract fun dailySummaryDao(): DailySummaryDao
}
