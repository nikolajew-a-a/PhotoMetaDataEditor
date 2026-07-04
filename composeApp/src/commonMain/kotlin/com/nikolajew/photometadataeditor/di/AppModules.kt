package com.nikolajew.photometadataeditor.di

import com.nikolajew.photometadataeditor.data.db.DatabaseDriverFactory
import com.nikolajew.photometadataeditor.data.db.PhotoIndexDatabase
import com.nikolajew.photometadataeditor.data.db.PhotoIndexLocalDataSource
import com.nikolajew.photometadataeditor.data.repository.PhotoLibraryRepositoryImpl
import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository
import com.nikolajew.photometadataeditor.domain.usecase.ObserveLibraryUseCase
import com.nikolajew.photometadataeditor.domain.usecase.OpenFolderUseCase
import com.nikolajew.photometadataeditor.domain.usecase.SetProcessedUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val domainModule: Module = module {
    factory { OpenFolderUseCase(get()) }
    factory { ObserveLibraryUseCase(get()) }
    factory { SetProcessedUseCase(get()) }
}

val dataModule: Module = module {
    single { PhotoIndexDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { PhotoIndexLocalDataSource(get()) }
    single<PhotoLibraryRepository> { PhotoLibraryRepositoryImpl(get(), get(), get()) }
}

val appModules: List<Module> = listOf(domainModule, dataModule)
