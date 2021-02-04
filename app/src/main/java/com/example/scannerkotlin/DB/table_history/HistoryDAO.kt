package com.example.scannerkotlin.DB.table_history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

/**
 * Created by ymKwon on 2021-02-02 오후 8:15.
 */
@Dao
interface HistoryDAO {

    @Insert(onConflict = REPLACE)
    fun insert(hist : HistoryEntity)

    @Query("SELECT * FROM history ORDER BY date DESC")
    fun getAllHistories() : List<HistoryEntity>

    @Delete
    fun delete(hist : HistoryEntity)
}