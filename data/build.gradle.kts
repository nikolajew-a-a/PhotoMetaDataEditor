plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(17)

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            api(project(":domain"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        desktopMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            // разбор JSON-вывода exiftool
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

sqldelight {
    databases {
        create("PhotoIndexDatabase") {
            packageName.set("com.nikolajew.photometadataeditor.data.db")
            // ON CONFLICT DO UPDATE требует SQLite >= 3.24 (дефолтный диалект — 3.18)
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:${libs.versions.sqldelight.get()}")
            verifyMigrations.set(false)
        }
    }
}

// Задача проверки миграций падает из-за конфликта нативных библиотек sqlite-jdbc,
// а миграций в проекте пока нет
tasks.configureEach {
    if (name.startsWith("verify") && name.endsWith("Migration")) {
        enabled = false
    }
}
