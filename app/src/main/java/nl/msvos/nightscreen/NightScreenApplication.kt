package nl.msvos.nightscreen

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import nl.msvos.nightscreen.overlay.DimServiceCommands
import nl.msvos.nightscreen.overlay.DimServiceState

class NightScreenApplication : Application(), DefaultLifecycleObserver {
    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        AppVisibilityState.setVisible(true)
        if (DimServiceState.running.value) {
            DimServiceCommands.appVisible(this)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        AppVisibilityState.setVisible(false)
        if (DimServiceState.running.value) {
            DimServiceCommands.appHidden(this)
        }
    }
}
