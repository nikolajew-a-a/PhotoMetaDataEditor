package com.nikolajew.photometadataeditor.ui.library

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface LibraryComponent {

    val state: StateFlow<LibraryState>
}

data class LibraryState(
    val folderPath: String? = null,
)

class DefaultLibraryComponent(
    componentContext: ComponentContext,
) : LibraryComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(LibraryState())
    override val state: StateFlow<LibraryState> = _state
}
