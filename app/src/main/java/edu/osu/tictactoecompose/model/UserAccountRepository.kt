package edu.osu.tictactoecompose.model

import android.app.Application
import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import edu.osu.tictactoecompose.TicTacToeApplication
import kotlinx.coroutines.flow.Flow

/**
 * Single point of accessing UserAccount data in the app.
 *
 * Source: https://developer.android.com/codelabs/android-room-with-a-view
 *
 * Created by acc on 2021/08/04.
 */
@Keep
class UserAccountRepository(application: Application) {

    private var userAccountDao: UserAccountDao

    // Room executes all queries on a separate thread.
    // Observed LiveData notify the observer upon data change.
    var allUserAccounts: Flow<List<UserAccount>>
    private val classTag = javaClass.simpleName

    fun findUserAccountByName(account: UserAccount): LiveData<UserAccount> {
        return userAccountDao.findByName(account.name, account.password)
    }

    // You MUST call this on a non-UI thread or the app will throw an exception.
    // I'm passing a Runnable object to the database.
    @WorkerThread
	suspend fun insert(account: UserAccount) {
        userAccountDao.insert(account)
	}

	// Similarly, I'm calling update() on a non-UI thread.
	@WorkerThread
	suspend fun update(account: UserAccount) {
		userAccountDao.update(account)
	}

	// Similarly, I'm calling delete() on a non-UI thread.
	@WorkerThread
	suspend fun delete(account: UserAccount) {
		userAccountDao.delete(account)
	}

    init {
        val db: UserAccountDatabase = UserAccountDatabase.getDatabase(application,
            (application as TicTacToeApplication).applicationScope
        )
        userAccountDao = db.userAccountDao()
        allUserAccounts = userAccountDao.getAllUserAccounts()
    }
}
