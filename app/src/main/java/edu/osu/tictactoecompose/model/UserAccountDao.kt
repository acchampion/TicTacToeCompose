package edu.osu.tictactoecompose.model

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
@Keep
interface UserAccountDao {
    @Query("SELECT rowid, name, password FROM useraccount")
    fun getAllUserAccounts(): Flow<List<UserAccount>>

    @Query("SELECT rowid, name, password FROM useraccount WHERE name LIKE :name AND password LIKE :password LIMIT 1")
    fun findByName(name: String, password: String): LiveData<UserAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: UserAccount)

    @Update
    suspend fun update(account: UserAccount)

    @Delete
    suspend fun delete(account: UserAccount)
}
