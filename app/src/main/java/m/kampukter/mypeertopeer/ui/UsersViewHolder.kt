package m.kampukter.mypeertopeer.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.user_item.view.*
import m.kampukter.mypeertopeer.data.UserData

class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(result: UserData, itemClickListener: ItemClickListener<UserData>?) {

        with(itemView) {
            userTextView.text = result.userName
            setOnClickListener { itemClickListener?.invoke(result) }
        }
    }

}
