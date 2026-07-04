package com.nikolajew.photometadataeditor.di

import com.nikolajew.photometadataeditor.data.db.DatabaseDriverFactory
import com.nikolajew.photometadataeditor.data.db.EditLogLocalDataSource
import com.nikolajew.photometadataeditor.data.db.PhotoIndexDatabase
import com.nikolajew.photometadataeditor.data.db.PhotoIndexLocalDataSource
import com.nikolajew.photometadataeditor.data.repository.MetadataRepositoryImpl
import com.nikolajew.photometadataeditor.data.repository.PhotoLibraryRepositoryImpl
import com.nikolajew.photometadataeditor.domain.repository.MetadataRepository
import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository
import com.nikolajew.photometadataeditor.domain.usecase.DeletePhotoUseCase
import com.nikolajew.photometadataeditor.domain.usecase.ObserveLibraryUseCase
import com.nikolajew.photometadataeditor.domain.usecase.OpenFolderUseCase
import com.nikolajew.photometadataeditor.domain.usecase.SetProcessedUseCase
import com.nikolajew.photometadataeditor.domain.usecase.UpdateCaptureDateUseCase
import com.nikolajew.photometadataeditor.domain.usecase.UpdateLocationUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val domainModule: Module = module {
    factory { OpenFolderUseCase(get()) }
    factory { ObserveLibraryUseCase(get()) }
    factory { SetProcessedUseCase(get()) }
    factory { UpdateCaptureDateUseCase(get()) }
    factory { UpdateLocationUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
}

val dataModule: Module = module {
    single { PhotoIndexDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { PhotoIndexLocalDataSource(get()) }
    single { EditLogLocalDataSource(get()) }
    single<PhotoLibraryRepository> { PhotoLibraryRepositoryImpl(get(), get(), get(), get()) }
    single<MetadataRepository> { MetadataRepositoryImpl(get(), get(), get()) }
}

val appModules: List<Module> = listOf(domainModule, dataModule)
