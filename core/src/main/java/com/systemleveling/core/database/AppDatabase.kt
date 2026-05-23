package com.systemleveling.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.systemleveling.core.database.converter.AppTypeConverters
import com.systemleveling.core.database.dao.CalendarEventDao
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.ItemDao
import com.systemleveling.core.database.dao.TitleDao
import com.systemleveling.core.database.dao.FinanceDao
import com.systemleveling.core.database.dao.CourseDao
import com.systemleveling.core.database.dao.JournalDao
import com.systemleveling.core.database.dao.DailySummaryDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.CalendarEventEntity
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

@Database(entities = [UserEntity::class, StatEntity::class, QuestEntity::class, SkillEntity::class, ItemEntity::class, TitleEntity::class, TransactionEntity::class, CourseEntity::class, JournalEntity::class, DailySummaryEntity::class, CalendarEventEntity::class], version = 9, exportSchema = true)
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
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        /** v7→v8: added isStored column to items table */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN isStored INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v8→v9: new calendar_events table */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS calendar_events (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        emoji TEXT NOT NULL DEFAULT '📌',
                        baseDateMs INTEGER NOT NULL,
                        timeStart TEXT,
                        timeEnd TEXT,
                        recurrenceType TEXT NOT NULL DEFAULT 'NONE',
                        reminderMinutesBefore INTEGER NOT NULL DEFAULT 0,
                        colorHex TEXT NOT NULL DEFAULT '#4A9EFF',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}
