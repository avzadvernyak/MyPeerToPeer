package m.kampukter.mypeertopeer.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.mypeertopeer.R

typealias ItemClickListener<T> = (T) -> Unit

class UsersAdapter(
    private val itemClickListener: ItemClickListener<String>? = null
) : RecyclerView.Adapter<UsersViewHolder>() {
    private var models: List<String>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return models?.size ?: 0
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        models?.get(position)?.let { item ->
            holder.bind(item, itemClickListener)
        }
    }

    fun setList(list: List<String>) {
        this.models = list
        notifyDataSetChanged()
    }
}