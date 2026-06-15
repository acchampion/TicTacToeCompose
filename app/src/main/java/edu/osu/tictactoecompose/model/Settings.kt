package com.wiley.fordummies.androidsdk.tictactoe.model

import androidx.annotation.Keep

/**
 * Class for reading settings from SharedPreferences (now the DataStore).
 *
 * Created by adamcchampion on 2017/08/14.
 */

@Keep
object Settings {
	val NAME = "settings.db"
	val ERROR = "error"

	object Keys {
		val OPT_NAME = "name"
		val OPT_NAME_DEFAULT = ""
		val OPT_NAME_COMPUTER = "Android"
		val OPT_PLAY_FIRST = "human_starts"
		val OPT_PLAY_FIRST_DEF = true

        val OPT_ROWS = "rows"
        val OPT_COLS = "cols"
        val OPT_ROWS_DEFAULT = 3
        val OPT_COLS_DEFAULT = 3

		val SCOREPLAYERONEKEY = "ScorePlayerOne"
		val SCOREPLAYERTWOKEY = "ScorePlayerTwo"
		val GAMEKEY = "Game"

		val PREF_SEARCH_QUERY = "searchQuery"
		val PREF_LAST_RESULT_ID = "lastResultId"
		val PREF_IS_POLLING = "isPolling"
	}
}
