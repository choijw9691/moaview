package com.moaview.moaview_sdk.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.moaview.moaview_sdk.common.PageModeSettingType
import com.moaview.moaview_sdk.common.PageTurnSettingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
private val Context.dataStore by preferencesDataStore(name = "USER_SETTINGS")

class UserSettingDataStore(val context: Context) {


    private val PAGE_TURN_SETTING_TYPE = stringPreferencesKey("PAGE_TURN_SETTING_TYPE")
    private val TWO_PAGE_MODE_IN_ORIENTATION = booleanPreferencesKey("TWO_PAGE_MODE_IN_ORIENTATION")
    private val TWO_PAGE_MODE_IN_FIRST_PAGE = booleanPreferencesKey("TWO_PAGE_MODE_IN_FIRST_PAGE")
    private val HIDE_SOFT_KEY = booleanPreferencesKey("HIDE_SOFT_KEY")
    private val PAGETURN_VOLUME_KEY = booleanPreferencesKey("PAGETURN_VOLUME_KEY")
    private val SCREEN_ALWAYS_ON = booleanPreferencesKey("SCREEN_ALWAYS_ON")
    private val FIXED_SCREEN_ROTATION = booleanPreferencesKey("FIXED_SCREEN_ROTATION")
    private val PAGE_MODE = stringPreferencesKey("PAGE_MODE")
    private val PAGE_COLOR_TYPE = intPreferencesKey("PAGE_COLOR_TYPE")
    private val SYSTEM_BRIGHTNESS_CHECK = booleanPreferencesKey("SYSTEM_BRIGHTNESS_CHECK")
    private val APP_BRIGHTNESS_VALUE = floatPreferencesKey("APP_BRIGHTNESS_VALUE")
    val pageMode: Flow<String> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[PAGE_MODE] ?: PageModeSettingType.SLIDE.toString()
        }

    suspend fun setPageMode(pageMode: PageModeSettingType) {
        context.dataStore.edit { preferences ->
            preferences[PAGE_MODE] = pageMode.toString()
        }
    }

    val pageTurnSettingType: Flow<String> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[PAGE_TURN_SETTING_TYPE] ?: PageTurnSettingType.BEFORE_NEXT.toString()
        }

    suspend fun setPageTurnSettingType(pageTurnSettingType: PageTurnSettingType) {
        context.dataStore.edit { preferences ->
            preferences[PAGE_TURN_SETTING_TYPE] = pageTurnSettingType.toString()
        }
    }

    val twoPageModeInOrientation: Flow<Boolean> = context.dataStore.data
           .catch { exception->
               if(exception is IOException) {
                   emit(emptyPreferences())
               } else {
                   throw exception
               }
           }.map { preference->
               preference[TWO_PAGE_MODE_IN_ORIENTATION] ?: false
           }

       suspend fun setTwoPageModeInOrientation(twoPageModeInOrientation: Boolean) {
           context.dataStore.edit { preferences ->
               preferences[TWO_PAGE_MODE_IN_ORIENTATION] = twoPageModeInOrientation
           }
       }

    val pageTurnVolumeKey: Flow<Boolean> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[PAGETURN_VOLUME_KEY] ?: false
        }

    suspend fun setPageTurnVolumeKey(pageTurnVolumeKey: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PAGETURN_VOLUME_KEY] = pageTurnVolumeKey
        }
    }

    val twoPageModeInFirstPage: Flow<Boolean> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[TWO_PAGE_MODE_IN_FIRST_PAGE] ?: true
        }

    suspend fun setTwoPageModeInFirstPage(twoPageModeInFirstPage: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TWO_PAGE_MODE_IN_FIRST_PAGE] = twoPageModeInFirstPage
        }
    }
    val hideSoftKey: Flow<Boolean> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[HIDE_SOFT_KEY] ?: true
        }

    suspend fun setHideSoftKey(hideSoftKey: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIDE_SOFT_KEY] = hideSoftKey
        }
    }

    val screenAlwaysOn: Flow<Boolean> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[SCREEN_ALWAYS_ON] ?: false
        }

    suspend fun setScreenAlwaysOn(screenAlwaysOn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SCREEN_ALWAYS_ON] = screenAlwaysOn
        }
    }

    val fixedScreenRotation: Flow<Boolean> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[FIXED_SCREEN_ROTATION] ?: false
        }

    suspend fun setFixedScreenRotation(fixedScreenRotation: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIXED_SCREEN_ROTATION] = fixedScreenRotation
        }
    }
    val pageColorType: Flow<Int> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[PAGE_COLOR_TYPE] ?: 0
        }

    suspend fun setPageColorType(pageColorType: Int) {
        context.dataStore.edit { preferences ->
            preferences[PAGE_COLOR_TYPE] = pageColorType
        }
    }
    val systemBrightNessCheck: Flow<Boolean> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[SYSTEM_BRIGHTNESS_CHECK] ?: true
        }

    suspend fun setSystemBrightNessCheck(isCheck: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYSTEM_BRIGHTNESS_CHECK] = isCheck
        }
    }

    val appBrightNessValue: Flow<Float> = context.dataStore.data
        .catch { exception->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference->
            preference[APP_BRIGHTNESS_VALUE] ?: 50
        } as Flow<Float>

    suspend fun setAppBrightNessValue(value: Float) {
        context.dataStore.edit { preferences ->
            preferences[APP_BRIGHTNESS_VALUE] = value
        }
    }
}

