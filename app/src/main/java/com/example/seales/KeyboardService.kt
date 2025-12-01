package com.example.seales

import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class KeyboardService : InputMethodService() {

    private val vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    private val audio by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }

    private lateinit var lifecycleOwner: KeyboardLifecycleOwner

    override fun onCreateInputView(): View {
        lifecycleOwner = KeyboardLifecycleOwner()

        val composeView = ComposeView(this)

        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        val coroutineContext = AndroidUiDispatcher.Main
        val recomposer = Recomposer(coroutineContext)

        composeView.compositionContext = recomposer

        CoroutineScope(coroutineContext).launch {
            recomposer.runRecomposeAndApplyChanges()
        }

        composeView.setContent {
            TecladoPersonalizado(
                onLetraClick = { letra ->
                    currentInputConnection?.commitText(letra, 1)
                    vibrar()
                    sonar()
                },
                onBorrarClick = {
                    currentInputConnection?.deleteSurroundingText(1, 0)
                    vibrar()
                },
                onCompartirClick = {}
            )
        }

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return composeView
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::lifecycleOwner.isInitialized) {
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    private fun vibrar() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, 80))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(40)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sonar() {
        try {
            audio.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean = false
}

class KeyboardLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    init {
        savedStateRegistryController.performRestore(null)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}