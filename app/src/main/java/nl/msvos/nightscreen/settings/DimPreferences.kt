package nl.msvos.nightscreen.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import nl.msvos.nightscreen.overlay.BrightnessMapper

private val Context.dimDataStore by preferencesDataStore(name = "dim_preferences")

data class DimSettings(
    val brightnessTenths: Int,
    val blueLightFilterEnabled: Boolean,
    val blueLightFilterStrength: Int,
    val autoStopInBrightLight: Boolean,
    val brightLightThresholdLux: Int,
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
                brightnessTenths = preferences[BRIGHTNESS_TENTHS]
                    ?.coerceIn(
                        BrightnessMapper.MIN_BRIGHTNESS,
                        BrightnessMapper.MAX_BRIGHTNESS,
                    )
                    ?: DEFAULT_BRIGHTNESS_TENTHS,
                blueLightFilterEnabled = preferences[BLUE_LIGHT_FILTER_ENABLED] ?: false,
                blueLightFilterStrength = preferences[BLUE_LIGHT_FILTER_STRENGTH]
                    ?.coerceIn(MIN_BLUE_LIGHT_FILTER_STRENGTH, MAX_BLUE_LIGHT_FILTER_STRENGTH)
                    ?: DEFAULT_BLUE_LIGHT_FILTER_STRENGTH,
                autoStopInBrightLight = preferences[AUTO_STOP_IN_BRIGHT_LIGHT] ?: false,
                brightLightThresholdLux = preferences[BRIGHT_LIGHT_THRESHOLD_LUX]
                    ?.coerceIn(MIN_BRIGHT_LIGHT_THRESHOLD_LUX, MAX_BRIGHT_LIGHT_THRESHOLD_LUX)
                    ?: DEFAULT_BRIGHT_LIGHT_THRESHOLD_LUX,
            )
        }

    suspend fun migrateBrightnessScale() {
        dataStore.edit { preferences ->
            if (preferences[BRIGHTNESS_TENTHS] == null) {
                preferences[BRIGHTNESS_TENTHS] = preferences[BRIGHTNESS_PERCENT]
                    ?.let(::percentToTenths)
                    ?: preferences[LEGACY_DIM_PERCENT]
                        ?.let(::legacyDimToBrightnessTenths)
                    ?: DEFAULT_BRIGHTNESS_TENTHS
            }
            preferences.remove(BRIGHTNESS_PERCENT)
            preferences.remove(LEGACY_DIM_PERCENT)
        }
    }

    suspend fun saveBrightnessTenths(brightnessTenths: Int) {
        dataStore.edit { preferences ->
            preferences[BRIGHTNESS_TENTHS] = brightnessTenths.coerceIn(
                BrightnessMapper.MIN_BRIGHTNESS,
                BrightnessMapper.MAX_BRIGHTNESS,
            )
        }
    }

    suspend fun saveBlueLightFilterEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BLUE_LIGHT_FILTER_ENABLED] = enabled
        }
    }

    suspend fun saveBlueLightFilterStrength(strength: Int) {
        dataStore.edit { preferences ->
            preferences[BLUE_LIGHT_FILTER_STRENGTH] = strength.coerceIn(
                MIN_BLUE_LIGHT_FILTER_STRENGTH,
                MAX_BLUE_LIGHT_FILTER_STRENGTH,
            )
        }
    }

    suspend fun saveAutoStopInBrightLight(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_STOP_IN_BRIGHT_LIGHT] = enabled
        }
    }

    suspend fun saveBrightLightThresholdLux(thresholdLux: Int) {
        dataStore.edit { preferences ->
            preferences[BRIGHT_LIGHT_THRESHOLD_LUX] = thresholdLux.coerceIn(
                MIN_BRIGHT_LIGHT_THRESHOLD_LUX,
                MAX_BRIGHT_LIGHT_THRESHOLD_LUX,
            )
        }
    }

    companion object {
        const val DEFAULT_BRIGHTNESS_TENTHS = 400
        const val DEFAULT_BLUE_LIGHT_FILTER_STRENGTH = 50
        const val MIN_BLUE_LIGHT_FILTER_STRENGTH = 0
        const val MAX_BLUE_LIGHT_FILTER_STRENGTH = 100
        const val DEFAULT_BRIGHT_LIGHT_THRESHOLD_LUX = 20
        const val MIN_BRIGHT_LIGHT_THRESHOLD_LUX = 5
        const val MAX_BRIGHT_LIGHT_THRESHOLD_LUX = 500

        internal fun percentToTenths(brightnessPercent: Int): Int =
            (brightnessPercent * 10).coerceIn(
                BrightnessMapper.MIN_BRIGHTNESS,
                BrightnessMapper.MAX_BRIGHTNESS,
            )

        internal fun legacyDimToBrightnessTenths(dimPercent: Int): Int =
            ((100 - dimPercent.coerceIn(0, 100)) * 10).coerceIn(
                BrightnessMapper.MIN_BRIGHTNESS,
                BrightnessMapper.MAX_BRIGHTNESS,
            )

        private val BRIGHTNESS_TENTHS: Preferences.Key<Int> =
            intPreferencesKey("brightness_tenths")
        private val BRIGHTNESS_PERCENT: Preferences.Key<Int> =
            intPreferencesKey("brightness_percent")
        private val BLUE_LIGHT_FILTER_ENABLED: Preferences.Key<Boolean> =
            booleanPreferencesKey("blue_light_filter_enabled")
        private val BLUE_LIGHT_FILTER_STRENGTH: Preferences.Key<Int> =
            intPreferencesKey("blue_light_filter_strength")
        private val AUTO_STOP_IN_BRIGHT_LIGHT: Preferences.Key<Boolean> =
            booleanPreferencesKey("auto_stop_in_bright_light")
        private val BRIGHT_LIGHT_THRESHOLD_LUX: Preferences.Key<Int> =
            intPreferencesKey("bright_light_threshold_lux")
        private val LEGACY_DIM_PERCENT: Preferences.Key<Int> =
            intPreferencesKey("dim_percent")
    }
}
