package com.example.scannerkotlin.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scannerkotlin.DB.table_history.HistoryEntity
import com.example.scannerkotlin.Interface.OnClickListener
import com.example.scannerkotlin.R
import kotlinx.android.synthetic.main.list_adapter_item.view.*

/**
 * Created by ymKwon on 2021-02-02 오후 8:05.
 */
class HistAdapter(
    val context: Context,
    var list: List<HistoryEntity>,
    var onClickListener: OnClickListener
) :
    RecyclerView.Adapter<HistAdapter.HistViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.list_adapter_item, parent, false)

        return HistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistViewHolder, position: Int) {
        val hist = list[position]

        holder.qrCode.text = hist.qrCode
        holder.date.text = hist.date
        holder.root.setOnClickListener {
            onClickListener.onClickListener(hist)
        }
    }

    inner class HistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val qrCode = itemView.qrCode
        val date = itemView.date
        val root = itemView.root
    }


}