package com.nikolajew.photometadataeditor.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.nikolajew.photometadataeditor.ui.library.DefaultLibraryComponent
import com.nikolajew.photometadataeditor.ui.library.LibraryComponent
import com.nikolajew.photometadataeditor.ui.map.DefaultMapOverviewComponent
import com.nikolajew.photometadataeditor.ui.map.MapOverviewComponent
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    fun onLibraryTabClick()

    fun onMapTabClick()

    sealed class Child {
        class Library(val component: LibraryComponent) : Child()
        class Map(val component: MapOverviewComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, KoinComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Library,
            handleBackButton = true,
            childFactory = ::child,
        )

    override fun onLibraryTabClick() {
        navigation.bringToFront(Config.Library)
    }

    override fun onMapTabClick() {
        navigation.bringToFront(Config.Map)
    }

    private fun child(
        config: Config,
        componentContext: ComponentContext,
    ): RootComponent.Child = when (config) {
        is Config.Library -> RootComponent.Child.Library(
            DefaultLibraryComponent(
                componentContext = componentContext,
                folderPicker = get(),
                openFolder = get(),
                setProcessed = get(),
                updateCaptureDate = get(),
                updateLocation = get(),
                deletePhoto = get(),
                observeLibrary = get(),
            ),
        )
        is Config.Map -> RootComponent.Child.Map(
            DefaultMapOverviewComponent(
                componentContext = componentContext,
                observeLibrary = get(),
            ),
        )
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Library : Config

        @Serializable
        data object Map : Config
    }
}
