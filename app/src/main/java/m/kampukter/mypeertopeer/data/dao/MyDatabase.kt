package m.kampukter.mypeertopeer.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,exportSchema = false, entities = [
        UsersInfo::class
    ]
)

abstract class MyDatabase : RoomDatabase() {

    abstract fun usersInfoDao(): UsersInfoDao

}
