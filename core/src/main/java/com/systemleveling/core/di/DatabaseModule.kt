package com.systemleveling.core.di

import android.content.Context
import androidx.room.Room
import com.systemleveling.core.database.AppDatabase
import com.systemleveling.core.database.dao.CalendarEventDao
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.SkillDao
import com.systemleveling.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "system_leveling.db"
        )
        .addMigrations(AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14)
        .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)
        .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideQuestDao(database: AppDatabase): QuestDao {
        return database.questDao()
    }

    @Provides
    fun provideSkillDao(database: AppDatabase): SkillDao {
        return database.skillDao()
    }

    @Provides
    fun provideItemDao(database: AppDatabase): com.systemleveling.core.database.dao.ItemDao {
        return database.itemDao()
    }

    @Provides
    fun provideTitleDao(database: AppDatabase): com.systemleveling.core.database.dao.TitleDao {
        return database.titleDao()
    }

    @Provides
    fun provideFinanceDao(database: AppDatabase): com.systemleveling.core.database.dao.FinanceDao {
        return database.financeDao()
    }

    @Provides
    fun provideCourseDao(database: AppDatabase): com.systemleveling.core.database.dao.CourseDao {
        return database.courseDao()
    }

    @Provides
    fun provideJournalDao(database: AppDatabase): com.systemleveling.core.database.dao.JournalDao {
        return database.journalDao()
    }

    @Provides
    fun provideDailySummaryDao(database: AppDatabase): com.systemleveling.core.database.dao.DailySummaryDao {
        return database.dailySummaryDao()
    }

    @Provides
    fun provideCalendarEventDao(database: AppDatabase): CalendarEventDao {
        return database.calendarEventDao()
    }

    @Provides
    fun provideBudgetDao(database: AppDatabase): com.systemleveling.core.database.dao.BudgetDao {
        return database.budgetDao()
    }

    @Provides
    fun provideDebtDao(database: AppDatabase): com.systemleveling.core.database.dao.DebtDao {
        return database.debtDao()
    }

    @Provides
    fun provideLessonDao(database: AppDatabase): com.systemleveling.core.database.dao.LessonDao {
        return database.lessonDao()
    }
}
