package com.moaview

import android.app.Application
import android.content.Context
import com.moaview.moaview.db.AppDatabase
import com.moaview.moaview.db.ContentsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class ViewerApplication: Application() {

/*    init{
        instance = this
    }

    companion object {
        lateinit var instance: ViewerApplication
        fun getApplicationContext() : Context {
            return instance.applicationContext
        }
    }

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase() }
    val repository by lazy { ContentsRepository(database.dao()) }*/
}