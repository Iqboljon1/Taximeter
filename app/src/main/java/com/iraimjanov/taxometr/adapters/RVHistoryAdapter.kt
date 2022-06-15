package com.iraimjanov.taxometr.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iraimjanov.taxometr.databinding.ItemHistoryBinding
import com.iraimjanov.taxometr.models.History

class RVHistoryAdapter(private val listHistory: ArrayList<History>) :
    RecyclerView.Adapter<RVHistoryAdapter.VH>() {

    inner class VH(private var itemRV: ItemHistoryBinding) : RecyclerView.ViewHolder(itemRV.root) {
        fun onBind(history: History) {
            itemRV.tvKmSumma.text = history.kmSumma
            itemRV.tvKm.text = history.km
            itemRV.tvSumma.text = history.summa
            itemRV.tvTime.text = history.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(listHistory[position])

    }

    override fun getItemCount(): Int = listHistory.size

}