package m.kampukter.mypeertopeer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.mypeertopeer.R
import m.kampukter.mypeertopeer.data.UserData

typealias ItemClickListener<T> = (T) -> Unit

class UsersAdapter(
    private val itemClickListener: ItemClickListener<UserData>? = null
) : RecyclerView.Adapter<UsersViewHolder>() {
    private var calledUsers: List<UserData>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return calledUsers?.size ?: 0
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        calledUsers?.get(position)?.let { item ->
            holder.bind(item, itemClickListener)
        }
    }

    fun setList(list: List<UserData>) {
        this.calledUsers = list
        notifyDataSetChanged()
    }
}