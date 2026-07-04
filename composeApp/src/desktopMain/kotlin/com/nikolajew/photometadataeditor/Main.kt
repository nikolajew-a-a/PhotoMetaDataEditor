package com.nikolajew.photometadataeditor

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.nikolajew.photometadataeditor.di.appModules
import com.nikolajew.photometadataeditor.di.desktopModule
import com.nikolajew.photometadataeditor.ui.root.DefaultRootComponent
import org.koin.core.context.startKoin
import javax.swing.SwingUtilities

fun main() {
    startKoin {
        modules(appModules + desktopModule)
    }

    val lifecycle = LifecycleRegistry()

    // Decompose требует создавать компоненты в UI-потоке (на JVM это EDT)
    val root = runOnUiThread {
        DefaultRootComponent(DefaultComponentContext(lifecycle = lifecycle))
    }

    application {
        val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "PhotoMetaDataEditor",
        ) {
            App(root)
        }
    }
}

private fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var result: T? = null
    var error: Throwable? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }

    error?.let { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
