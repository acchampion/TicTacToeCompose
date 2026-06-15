package edu.osu.tictactoecompose

import android.app.Application
import android.content.pm.ApplicationInfo
import edu.osu.tictactoecompose.model.SettingsDataStore
import edu.osu.tictactoecompose.model.UserAccountDatabase
import edu.osu.tictactoecompose.model.UserAccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class TicTacToeApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { UserAccountDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { UserAccountRepository(this) }

    val dataStore = SettingsDataStore(this, applicationScope)

    override fun onCreate() {
        super.onCreate()

        val isDebuggable = (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))

        if (isDebuggable) {
            val debugTree: Timber.DebugTree = Timber.DebugTree()
            Timber.plant(debugTree)
            // LeakCanary.setConfig(LeakCanary.getConfig());
        }

        //StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
        //StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
    }

}