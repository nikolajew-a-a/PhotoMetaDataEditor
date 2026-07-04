package com.nikolajew.photometadataeditor.di

import org.koin.core.module.Module
import org.koin.dsl.module

val domainModule: Module = module {
    // use case'ы появятся вместе со сканером папки
}

val dataModule: Module = module {
    // репозитории, БД
}

val appModules: List<Module> = listOf(domainModule, dataModule)
