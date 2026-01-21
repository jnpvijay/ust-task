package com.ust.mytask.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ust.mytask.R
import com.ust.mytask.model.MdnsDevice

class MdnsAdapter : RecyclerView.Adapter<MdnsAdapter.VH>() {

    private var list = emptyList<MdnsDevice>()

    var onItemClick: ((MdnsDevice) -> Unit)? = null

    fun submitList(newList: List<MdnsDevice>) {
        list = newList
        notifyDataSetChanged()
    }

    class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dns, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = list[position]
        holder.view.findViewById<TextView>(R.id.txtDeviceName).text = d.name
        holder.view.findViewById<TextView>(R.id.txtIpAddress).text = d.ip ?: "N/A"

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(d)
        }
    }

    override fun getItemCount() = list.size
}