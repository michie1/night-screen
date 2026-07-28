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
                brightnessTenths = preferences[BRIGHTNESS_TENTHS]
                    ?.coerceIn(
                        BrightnessMapper.MIN_BRIGHTNESS,
                        BrightnessMapper.MAX_BRIGHTNESS,
                    )
                    ?: DEFAULT_BRIGHTNESS_TENTHS,
                autoStopInBrightLight = preferences[AUTO_STOP_IN_BRIGHT_LIGHT] ?: false,
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

    suspend fun saveAutoStopInBrightLight(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_STOP_IN_BRIGHT_LIGHT] = enabled
        }
    }

    companion object {
        const val DEFAULT_BRIGHTNESS_TENTHS = 400

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
        private val AUTO_STOP_IN_BRIGHT_LIGHT: Preferences.Key<Boolean> =
            booleanPreferencesKey("auto_stop_in_bright_light")
        private val LEGACY_DIM_PERCENT: Preferences.Key<Int> =
            intPreferencesKey("dim_percent")
    }
}
