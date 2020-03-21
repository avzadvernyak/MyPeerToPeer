package m.kampukter.mypeertopeer.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.user_item.view.*

class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(result: String, itemClickListener: ItemClickListener<String>?) {

        with(itemView) {
            userTextView.text = result
            setOnClickListener { itemClickListener?.invoke(result) }
        }
    }

}
