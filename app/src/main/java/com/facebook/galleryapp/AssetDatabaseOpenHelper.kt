package com.facebook.galleryapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AssetDatabaseOpenHelper(private val context: Context, private val DB_NAME: String) {

    fun saveDatabase(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(DB_NAME)

        if (!dbFile.exists()) {
            try {
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }

        }

        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    @Throws(IOException::class)
    private fun copyDatabase(dbFile: File) {

        val `is` = context.assets.open(DB_NAME)
        val os = FileOutputStream(dbFile)

        val buffer = ByteArray(1024)
        while (`is`.read(buffer) > 0) {
            os.write(buffer)
        }

        os.flush()
        os.close()
        `is`.close()
    }

}