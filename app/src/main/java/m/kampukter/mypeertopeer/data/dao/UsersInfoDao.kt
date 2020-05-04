package m.kampukter.mypeertopeer.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsersInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expanses: UsersInfo): Long

    @Query("select * from users_info")
    fun getAll(): LiveData<List<UsersInfo>>
}
