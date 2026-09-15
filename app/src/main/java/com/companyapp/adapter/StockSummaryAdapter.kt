package com.companyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.companyapp.R
import com.companyapp.databinding.ItemStockSummaryBinding
import com.companyapp.models.StockItem

class StockSummaryAdapter(
    private val items: List<StockItem>,
    private val onClick: (StockItem) -> Unit
) : RecyclerView.Adapter<StockSummaryAdapter.VH>() {

    inner class VH(val binding: ItemStockSummaryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStockSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.binding
        b.tvItemName.text = item.name
        b.tvCategory.text = item.category.ifEmpty { "General" }
        b.tvQuantity.text = "${item.quantity} ${item.unit}"

        val isLow = item.quantity <= item.minQuantity
        b.tvStatus.text = if (isLow) "Low" else "OK"
        b.tvStatus.setBackgroundResource(if (isLow) R.drawable.bg_stock_tag_low else R.drawable.bg_stock_tag_in)
        b.tvStatus.setTextColor(
            b.root.context.getColor(if (isLow) R.color.low_stock_text else R.color.in_stock_text)
        )
        b.root.setOnClickListener { onClick(item) }
    }
}
