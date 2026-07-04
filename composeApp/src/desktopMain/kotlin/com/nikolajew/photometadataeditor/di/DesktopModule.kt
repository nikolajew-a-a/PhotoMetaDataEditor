package com.nikolajew.photometadataeditor.di

import com.nikolajew.photometadataeditor.data.db.DatabaseDriverFactory
import com.nikolajew.photometadataeditor.data.db.DesktopDatabaseDriverFactory
import com.nikolajew.photometadataeditor.data.scanner.DesktopMediaFileScanner
import com.nikolajew.photometadataeditor.data.scanner.MediaFileScanner
import com.nikolajew.photometadataeditor.platform.DesktopFolderPicker
import com.nikolajew.photometadataeditor.platform.FolderPicker
import org.koin.core.module.Module
import org.koin.dsl.module

val desktopModule: Module = module {
    single<DatabaseDriverFactory> { DesktopDatabaseDriverFactory() }
    single<MediaFileScanner> { DesktopMediaFileScanner() }
    single<FolderPicker> { DesktopFolderPicker() }
}
