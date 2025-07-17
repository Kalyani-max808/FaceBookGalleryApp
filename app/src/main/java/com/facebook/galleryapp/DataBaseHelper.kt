package com.facebook.galleryapp

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log


class DataBaseHelper(context: Context, db_name: String) : SQLiteOpenHelper(context, db_name, null, 1) {
//    var topicslist = getTopics()
var  dbase:SQLiteDatabase

    init {
         dbase = this.writableDatabase
    }

    override fun onOpen(db: SQLiteDatabase?) {

        dbase = db as SQLiteDatabase
        db.execSQL(SQL_CREATE_BOOKMARKS) //Create the TableDef table if it doesn't exist
//        deleteAllBookMarks() //FOR TESTING
        }

    override fun onCreate(db: SQLiteDatabase) {
        Log.e("DB","On create called")
        dbase = db
        db.execSQL(SQL_CREATE_BOOKMARKS) //Create the TableDef table if it doesn't exist
     }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun addBookMark(imgName:String){

         val SQL_BOOKMARK_INSERT =
                "INSERT or replace INTO ${TableDef.BookMark.TABLE_NAME} (${TableDef.BookMark.COLUMN_NAME}) VALUES('$imgName')"
        dbase.execSQL(SQL_BOOKMARK_INSERT)
    }
    fun deleteBookMark(imgName:String):Boolean{

        val table = TableDef.BookMark.TABLE_NAME
        val whereClause = "name=?"
        val whereArgs = arrayOf(imgName)
        return dbase.delete(table, whereClause, whereArgs)>0
   }
    fun deleteAllBookMarks():Boolean{
//        val db = this.writableDatabase

        val table = TableDef.BookMark.TABLE_NAME
        val whereClause = "name<>?"
        val whereArgs = arrayOf(" ")
        return dbase.delete(table, whereClause, whereArgs)>0
   }

    fun getAllData(tableName: String): Cursor {

        val db = this.writableDatabase

        return db.rawQuery("select * from $tableName", null)

    }

    fun getAllBookMarks(): ArrayList<String> {
        var list:ArrayList<String> = ArrayList()
        val qry = "select name from bookmarks"
        val cursor = this.readableDatabase.rawQuery(qry, null)
        if (cursor.moveToFirst()){
            do {
                list.add (
                       cursor.getString(0)
                )

            }while (cursor.moveToNext())
        }
        cursor.close()

        return list
    }
    fun getTopics():Collection<Topic?>{
        var list:ArrayList<Topic?> = ArrayList()

        val qry = "select ID, REMEDIE_NAME from REMEDIE_TYPE"
        val cursor = this.readableDatabase.rawQuery(qry, null)
        if (cursor.moveToFirst()){
            do {
                list.add (
                        Topic(cursor.getInt(0),cursor.getString(1))
                )

            }while (cursor.moveToNext())
        }
        cursor.close()

        return list
    }
    fun getMenus():Collection<Menu?>?{
        var menus:ArrayList<Menu?> = ArrayList()

        val qry = "select TYPE_ID,SUB_TYPE_ID,NAME from REMEDIES where TYPE_ID="+ItemDataset.TOPIC_ID
        val cursor = this.readableDatabase.rawQuery(qry, null)
        if (cursor.moveToFirst()){
            do {
                menus.add (
                        Menu(cursor.getInt(0),cursor.getInt(1),cursor.getString(2))
                )

            }while (cursor.moveToNext())
        }
        cursor.close()

        return menus
    }

    fun getItem():String{
        var itemText = "Nothing Found"

        val qry = "select DESCRIPTION from REMEDIES where TYPE_ID="+ItemDataset.TOPIC_ID.toString()+" and " +
                "SUB_TYPE_ID="+ItemDataset.MENU_ID
        val cursor = this.readableDatabase.rawQuery(qry, null)
        if (cursor.moveToFirst()){
             itemText = cursor.getString(0)
        }
        cursor.close()

        return itemText
    }

}

data class Topic(
        var ID:Int,
        var TOPIC_TITLE:String
        )
data class Menu(
        var TOPIC_ID:Int,
        var MENU_ID:Int,
        var MENU_TITLE:String
)
object TableDef {
    // Table contents are grouped together in an anonymous object.
    object BookMark : BaseColumns {
        const val TABLE_NAME = "bookmarks"
        const val COLUMN_NAME = "name"
    }
}

private const val SQL_CREATE_BOOKMARKS =
        "CREATE TABLE IF NOT EXISTS  ${TableDef.BookMark.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${TableDef.BookMark.COLUMN_NAME} TEXT," + " UNIQUE(${TableDef.BookMark.COLUMN_NAME}) )"


