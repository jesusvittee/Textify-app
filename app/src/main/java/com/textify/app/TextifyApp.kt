package com.textify.app

import android.app.Application
import androidx.room.Room
import com.textify.app.data.local.database.AppDatabase
import com.textify.app.utils.Constants

class TextifyApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}