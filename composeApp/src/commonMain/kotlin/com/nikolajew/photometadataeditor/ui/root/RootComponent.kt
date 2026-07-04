package com.nikolajew.photometadataeditor.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.nikolajew.photometadataeditor.ui.library.DefaultLibraryComponent
import com.nikolajew.photometadataeditor.ui.library.LibraryComponent
import kotlinx.serialization.Serializable

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Library(val component: LibraryComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Library,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(
        config: Config,
        componentContext: ComponentContext,
    ): RootComponent.Child = when (config) {
        is Config.Library -> RootComponent.Child.Library(
            DefaultLibraryComponent(componentContext),
        )
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Library : Config
    }
}
