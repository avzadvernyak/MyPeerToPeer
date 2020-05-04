package m.kampukter.mypeertopeer.data.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_info")

data class UsersInfo (
    @PrimaryKey
    val id : String,
    val userName: String,
    val isConnectedWS: Boolean
)
