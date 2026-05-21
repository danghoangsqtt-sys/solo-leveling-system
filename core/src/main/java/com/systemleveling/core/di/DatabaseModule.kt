package com.systemleveling.core.di

import android.content.Context
import androidx.room.Room
import com.systemleveling.core.database.AppDatabase
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
        .fallbackToDestructiveMigration()
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
}
