package com.kotlinjs5.jsvideoplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentFilesAdapter(
    private val files: List<RecentFile>,
    private val onClick: (RecentFile) -> Unit
) : RecyclerView.Adapter<RecentFilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFileName: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.tvFileName.text = file.name
        holder.itemView.setOnClickListener { onClick(file) }
    }

    override fun getItemCount() = files.size
}
