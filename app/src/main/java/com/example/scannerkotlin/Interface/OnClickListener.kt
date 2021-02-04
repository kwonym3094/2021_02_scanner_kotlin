package com.example.scannerkotlin.Interface

import com.example.scannerkotlin.DB.table_history.HistoryEntity

/**
 * Created by ymKwon on 2021-02-02 오후 9:05.
 */
interface OnClickListener {
    fun onClickListener(hist : HistoryEntity)
}