package com.tapbi.spark.yokey.di

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.data.local.db.ThemeDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideSharedPreference(context: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideRoomDb(context: Application?): ThemeDB {
        return Room.databaseBuilder(context!!, ThemeDB::class.java, Constant.DB_NAME)
            .fallbackToDestructiveMigration().addMigrations(ThemeDB.MIGRATION_1_2).build()
    }


}