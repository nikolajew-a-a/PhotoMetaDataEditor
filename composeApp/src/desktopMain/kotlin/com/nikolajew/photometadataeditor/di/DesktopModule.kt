package com.nikolajew.photometadataeditor.di

import com.nikolajew.photometadataeditor.data.db.DatabaseDriverFactory
import com.nikolajew.photometadataeditor.data.db.DesktopDatabaseDriverFactory
import com.nikolajew.photometadataeditor.data.filesystem.DesktopFileDeleter
import com.nikolajew.photometadataeditor.data.filesystem.DesktopFileExistenceChecker
import com.nikolajew.photometadataeditor.data.filesystem.FileDeleter
import com.nikolajew.photometadataeditor.data.filesystem.FileExistenceChecker
import com.nikolajew.photometadataeditor.data.metadata.ExifToolLocator
import com.nikolajew.photometadataeditor.data.metadata.ExifToolMetadataEngine
import com.nikolajew.photometadataeditor.data.metadata.ExifToolProcess
import com.nikolajew.photometadataeditor.data.metadata.MetadataEngine
import com.nikolajew.photometadataeditor.data.scanner.DesktopMediaFileScanner
import com.nikolajew.photometadataeditor.data.scanner.MediaFileScanner
import com.nikolajew.photometadataeditor.platform.DesktopFolderPicker
import com.nikolajew.photometadataeditor.platform.FolderPicker
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

val desktopModule: Module = module {
    single<DatabaseDriverFactory> { DesktopDatabaseDriverFactory() }
    single<MediaFileScanner> { DesktopMediaFileScanner() }
    single<FolderPicker> { DesktopFolderPicker() }
    single<FileDeleter> { DesktopFileDeleter() }
    single<FileExistenceChecker> { DesktopFileExistenceChecker() }
    single { ExifToolProcess(ExifToolLocator::locate) } onClose { it?.closeQuietly() }
    single<MetadataEngine> { ExifToolMetadataEngine(get()) }
}
