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
import com.systemleveling.core.database.dao.BudgetDao
import com.systemleveling.core.database.dao.DebtDao
import com.systemleveling.core.database.dao.LessonDao
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
import com.systemleveling.core.database.entity.BudgetEntity
import com.systemleveling.core.database.entity.DebtEntity
import com.systemleveling.core.database.entity.LessonEntity

@Database(entities = [UserEntity::class, StatEntity::class, QuestEntity::class, SkillEntity::class, ItemEntity::class, TitleEntity::class, TransactionEntity::class, CourseEntity::class, JournalEntity::class, DailySummaryEntity::class, CalendarEventEntity::class, BudgetEntity::class, DebtEntity::class, LessonEntity::class], version = 14, exportSchema = true)
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
    abstract fun budgetDao(): BudgetDao
    abstract fun debtDao(): DebtDao
    abstract fun lessonDao(): LessonDao

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

        /** v9→v10: budgets & debts tables */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        category TEXT NOT NULL PRIMARY KEY,
                        limitAmount INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS debts (
                        id TEXT NOT NULL PRIMARY KEY,
                        personName TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        isPaid INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /** v10→v11: lessons table + new course columns */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN contentUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE courses ADD COLUMN contentType TEXT NOT NULL DEFAULT 'GENERAL'")
                db.execSQL("ALTER TABLE courses ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE courses ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS lessons (
                        id TEXT NOT NULL PRIMARY KEY,
                        courseId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        contentUrl TEXT NOT NULL DEFAULT '',
                        contentType TEXT NOT NULL DEFAULT 'GENERAL',
                        orderIndex INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        /** v11→v12: parentId column for course folder nesting */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN parentId TEXT")
            }
        }

        /** v12→v13: profession, personalDescription, generatedAvatarBase64 on users */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN profession TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN personalDescription TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN generatedAvatarBase64 TEXT")
            }
        }

        /** v13→v14: performance indices on frequently queried columns */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_quests_date ON quests(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_lessons_courseId ON lessons(courseId)")
            }
        }
    }
}
