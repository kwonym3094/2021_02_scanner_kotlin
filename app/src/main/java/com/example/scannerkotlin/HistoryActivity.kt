package com.example.scannerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scannerkotlin.Adapters.HistAdapter
import com.example.scannerkotlin.DB.table_history.HistoryDatabase
import com.example.scannerkotlin.DB.table_history.HistoryEntity
import com.example.scannerkotlin.Interface.OnClickListener
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() , OnClickListener {

    lateinit var db : HistoryDatabase
    var histList = listOf<HistoryEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        db = HistoryDatabase.getInstance(this)!!

        recyclerView.layoutManager = LinearLayoutManager(this)

        getAllHistories()
    }

    fun getAllHistories(){
        GlobalScope.launch {
            histList = db.historyDAO().getAllHistories()
            setRecyclerView(histList)
        }
    }

    private fun setRecyclerView(histList : List<HistoryEntity>){
        recyclerView.adapter = HistAdapter(this,histList,this)
    }

    override fun onClickListener(hist: HistoryEntity) {
        val qrCode = hist.qrCode
        intent.putExtra("QRCODE",qrCode)
        setResult(RESULT_OK, intent)
        finish()
    }
}