package com.moaview.moaview.di

import android.content.Context
import androidx.room.Room
import com.moaview.moaview.dao.ContentsDao
import com.moaview.moaview.db.AppDatabase
import com.moaview.moaview.db.ContentsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideContentsDao(db: AppDatabase): ContentsDao {
        return db.dao()
    }

    @Singleton
    @Provides
    fun provideContentsRepository(dao: ContentsDao) : ContentsRepository {
        return ContentsRepository(dao)
    }
}
