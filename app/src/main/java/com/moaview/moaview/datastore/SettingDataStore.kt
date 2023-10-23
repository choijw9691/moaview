package com.moaview.moaview.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.moaview.moaview.common.ListModeEnum
import com.moaview.moaview.common.SortState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "SETTINGS")
class SettingDataStore (val context: Context) {

    private val LIST_MODE_TYPE = stringPreferencesKey("ListMode")
    private val SORT_STATE_TYPE = stringPreferencesKey("SortState")
    private val TUTORIAL_READ_CHECK = booleanPreferencesKey("TutorialRead")

    val listMode: Flow<String> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[LIST_MODE_TYPE] ?: ListModeEnum.COVERVIEW.toString()
        }

    suspend fun setListMode(listMode: ListModeEnum) {
        context.dataStore.edit { preferences ->
            preferences[LIST_MODE_TYPE] = listMode.toString()
        }
    }

    val sortState: Flow<String> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[SORT_STATE_TYPE] ?: SortState.READ.toString()
        }

    suspend fun setSortState(sortState: SortState) {
        context.dataStore.edit { preferences ->
            preferences[SORT_STATE_TYPE] = sortState.toString()
        }
    }

    val tutorialState: Flow<Boolean> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[TUTORIAL_READ_CHECK] ?: false
        }

    suspend fun setTutorialState(isChecked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TUTORIAL_READ_CHECK] = isChecked
        }
    }

}
