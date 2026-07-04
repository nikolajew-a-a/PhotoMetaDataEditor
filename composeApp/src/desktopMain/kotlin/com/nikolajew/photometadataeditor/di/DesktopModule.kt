package com.nikolajew.photometadataeditor.di

import org.koin.core.module.Module
import org.koin.dsl.module

val desktopModule: Module = module {
    // desktop-реализации: ExifToolClient, FolderPicker, ThumbnailGenerator
}
