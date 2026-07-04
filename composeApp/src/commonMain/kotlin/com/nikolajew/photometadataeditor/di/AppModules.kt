package com.nikolajew.photometadataeditor.di

import com.nikolajew.photometadataeditor.data.repository.PhotoLibraryRepositoryImpl
import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository
import com.nikolajew.photometadataeditor.domain.usecase.ObserveLibraryUseCase
import com.nikolajew.photometadataeditor.domain.usecase.OpenFolderUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val domainModule: Module = module {
    factory { OpenFolderUseCase(get()) }
    factory { ObserveLibraryUseCase(get()) }
}

val dataModule: Module = module {
    single<PhotoLibraryRepository> { PhotoLibraryRepositoryImpl(get()) }
}

val appModules: List<Module> = listOf(domainModule, dataModule)
