package edu.osu.tictactoecompose.model

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

/**
 * Database class for UserAccount processing with Room.
 *
 * Source: https://developer.android.com/codelabs/android-room-with-a-view
 *
 * Created by acc on 2021/08/04.
 */
@Keep
@Database(entities = [UserAccount::class], version = 1, exportSchema = false)
abstract class UserAccountDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("Not yet implemented")
    }

    override fun clearAllTables() {
        TODO("Not yet implemented")
    }

    companion object {
        // Singleton prevents multiple instances of database open at the same time.
        @Volatile
        private var INSTANCE: UserAccountDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): UserAccountDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserAccountDatabase::class.java,
                    "useraccount_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}