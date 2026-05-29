package com.example.taskerine_v2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.taskerine_v2.data.local.dao.ReviewDao
import com.example.taskerine_v2.data.local.dao.TaskDao
import com.example.taskerine_v2.data.local.dao.UserDao
import com.example.taskerine_v2.data.local.entities.TaskEntity
import com.example.taskerine_v2.data.local.entities.UserEntity
import com.example.taskerine_v2.data.local.entities.ReviewEntity
import com.example.taskerine_v2.data.model.Role
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TaskEntity::class, UserEntity::class, ReviewEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskerineDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile private var INSTANCE: TaskerineDatabase? = null

        fun getInstance(context: Context): TaskerineDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TaskerineDatabase::class.java,
                    "taskerine_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default users on first run
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.userDao()?.insertAll(
                                    listOf(
                                        UserEntity("u1", "alice", "alice@email.com", Role.REQUESTER, 200),
                                        UserEntity("u2", "bob", "bob@email.com", Role.TASKER, 100)
                                    )
                                )
                                INSTANCE?.taskDao()?.insertAll(
                                    listOf(
                                        TaskEntity(
                                            id = "t1",
                                            title = "Help move furniture",
                                            description = "Need help moving a sofa and dining table to a new flat nearby.",
                                            location = "Brixton, London",
                                            reward = 40.0,
                                            requesterId = "u1",
                                            requesterName = "alice"
                                        ),
                                        TaskEntity(
                                            id = "t2",
                                            title = "Grocery shopping",
                                            description = "Pick up a list of groceries from Sainsbury's and drop off.",
                                            location = "Hackney, London",
                                            reward = 15.0,
                                            requesterId = "u1",
                                            requesterName = "alice"
                                        )
                                    )
                                )
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

