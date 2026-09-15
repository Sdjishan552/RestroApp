package com.companyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.companyapp.R
import com.companyapp.databinding.ItemStockBinding
import com.companyapp.models.StockItem

class StockAdapter(
    private val items: List<StockItem>,
    private val onEdit: (StockItem) -> Unit,
    private val onDelete: (StockItem) -> Unit
) : RecyclerView.Adapter<StockAdapter.VH>() {

    inner class VH(val binding: ItemStockBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.tvItemName.text = item.name
        b.tvCategory.text = item.category.ifEmpty { "General" }
        b.tvQuantity.text = "${item.quantity} ${item.unit}"
        b.tvPrice.text = "₹%.2f / ${item.unit}".format(item.price)
        b.tvTotalValue.text = "Value: ₹%.2f".format(item.quantity * item.price)

        val isLow = item.quantity <= item.minQuantity
        b.tvStatus.text = if (isLow) "⚠ Low Stock" else "✅ In Stock"
        b.tvStatus.setBackgroundResource(if (isLow) R.drawable.bg_stock_tag_low else R.drawable.bg_stock_tag_in)
        b.tvStatus.setTextColor(
            holder.binding.root.context.getColor(
                if (isLow) R.color.low_stock_text else R.color.in_stock_text
            )
        )

        b.btnEdit.setOnClickListener { onEdit(item) }
        b.btnDelete.setOnClickListener { onDelete(item) }
    }
}
