package com.example.scannerkotlin.DB.table_history

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by ymKwon on 2021-02-02 오후 8:10.
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var qrCode: String,
    var date: String
)