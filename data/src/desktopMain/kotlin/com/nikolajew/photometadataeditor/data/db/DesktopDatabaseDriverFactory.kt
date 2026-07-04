package com.nikolajew.photometadataeditor.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

class DesktopDatabaseDriverFactory : DatabaseDriverFactory {

    override fun createDriver(): SqlDriver {
        val appDataDir = File(
            System.getenv("APPDATA") ?: System.getProperty("user.home"),
            "PhotoMetaDataEditor",
        )
        appDataDir.mkdirs()
        val dbFile = File(appDataDir, "photoindex.db")

        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            properties = Properties(),
            schema = PhotoIndexDatabase.Schema,
        )
    }
}
