package com.superlist.app.ui.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class SimpleAdapter(
    private val items: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

    class ViewHolder(val card: MaterialCardView, val textView: MaterialTextView) :
        RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val card = MaterialCardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            radius = 12f
            cardElevation = 2f
            setContentPadding(24, 20, 24, 20)
        }
        val textView = MaterialTextView(context).apply {
            textSize = 16f
            setPadding(0, 0, 0, 0)
        }
        card.addView(textView)
        return ViewHolder(card, textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = items[position]
        holder.card.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount() = items.size
}
