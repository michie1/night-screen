package nl.msvos.nightscreen.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dimDataStore by preferencesDataStore(name = "dim_preferences")

class DimPreferences(context: Context) {
    private val dataStore = context.applicationContext.dimDataStore

    val dimPercent: Flow<Int> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            preferences[DIM_PERCENT]
                ?.coerceIn(MIN_DIM_PERCENT, MAX_DIM_PERCENT)
                ?: DEFAULT_DIM_PERCENT
        }

    suspend fun saveDimPercent(percent: Int) {
        dataStore.edit { preferences: androidx.datastore.preferences.core.MutablePreferences ->
            preferences[DIM_PERCENT] = percent.coerceIn(MIN_DIM_PERCENT, MAX_DIM_PERCENT)
        }
    }

    companion object {
        const val MIN_DIM_PERCENT = 0
        const val MAX_DIM_PERCENT = 100
        const val DEFAULT_DIM_PERCENT = 60

        private val DIM_PERCENT: Preferences.Key<Int> = intPreferencesKey("dim_percent")
    }
}
