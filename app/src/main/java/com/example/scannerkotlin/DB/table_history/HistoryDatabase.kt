package com.example.scannerkotlin.DB.table_history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Created by ymKwon on 2021-02-02 오후 8:20.
 */
@Database(entities = arrayOf(HistoryEntity::class), version = 1)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDAO(): HistoryDAO

    companion object {
        var INSTANCE: HistoryDatabase? = null

        fun getInstance(context: Context) : HistoryDatabase? {
            if (INSTANCE == null) {
                synchronized(HistoryDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        HistoryDatabase::class.java, "history.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return INSTANCE
        }
    }
}