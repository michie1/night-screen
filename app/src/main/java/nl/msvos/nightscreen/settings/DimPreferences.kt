package nl.msvos.nightscreen.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import nl.msvos.nightscreen.overlay.BrightnessMapper

private val Context.dimDataStore by preferencesDataStore(name = "dim_preferences")

data class DimSettings(
    val brightnessPercent: Int,
    val autoStopInBrightLight: Boolean,
)

class DimPreferences(context: Context) {
    private val dataStore = context.applicationContext.dimDataStore

    val settings: Flow<DimSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            DimSettings(
                brightnessPercent = preferences[BRIGHTNESS_PERCENT]
                    ?.coerceIn(
                        BrightnessMapper.MIN_BRIGHTNESS,
                        BrightnessMapper.MAX_BRIGHTNESS,
                    )
                    ?: DEFAULT_BRIGHTNESS_PERCENT,
                autoStopInBrightLight = preferences[AUTO_STOP_IN_BRIGHT_LIGHT] ?: false,
            )
        }

    suspend fun migrateLegacyDimPercent() {
        dataStore.edit { preferences ->
            if (preferences[BRIGHTNESS_PERCENT] == null) {
                preferences[BRIGHTNESS_PERCENT] = preferences[LEGACY_DIM_PERCENT]
                    ?.let(::legacyDimToBrightness)
                    ?: DEFAULT_BRIGHTNESS_PERCENT
            }
            preferences.remove(LEGACY_DIM_PERCENT)
        }
    }

    suspend fun saveBrightnessPercent(brightnessPercent: Int) {
        dataStore.edit { preferences ->
            preferences[BRIGHTNESS_PERCENT] = brightnessPercent.coerceIn(
                BrightnessMapper.MIN_BRIGHTNESS,
                BrightnessMapper.MAX_BRIGHTNESS,
            )
        }
    }

    suspend fun saveAutoStopInBrightLight(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_STOP_IN_BRIGHT_LIGHT] = enabled
        }
    }

    companion object {
        const val DEFAULT_BRIGHTNESS_PERCENT = 40

        internal fun legacyDimToBrightness(dimPercent: Int): Int =
            (
                BrightnessMapper.MAX_BRIGHTNESS -
                    dimPercent.coerceIn(0, 100) / 100f *
                    (BrightnessMapper.MAX_BRIGHTNESS - BrightnessMapper.MIN_BRIGHTNESS)
                )
                .roundToInt()
                .coerceIn(
                    BrightnessMapper.MIN_BRIGHTNESS,
                    BrightnessMapper.MAX_BRIGHTNESS,
                )

        private val BRIGHTNESS_PERCENT: Preferences.Key<Int> =
            intPreferencesKey("brightness_percent")
        private val AUTO_STOP_IN_BRIGHT_LIGHT: Preferences.Key<Boolean> =
            booleanPreferencesKey("auto_stop_in_bright_light")
        private val LEGACY_DIM_PERCENT: Preferences.Key<Int> =
            intPreferencesKey("dim_percent")
    }
}
