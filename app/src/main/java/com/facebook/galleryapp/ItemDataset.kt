package com.facebook.galleryapp

import java.io.File


object ItemDataset {
    var position:Int = 0
    var position_bookmark:Int = 0
    var topics = Images_Topics.topic_titles
    var topics_icons:ArrayList<String> = arrayListOf()
    var APP_DIR:File? = null


    var items:ArrayList<Item> = arrayListOf()

    var TOPIC_ID:Int = 1
    var MENU_ID:Int = 1
    var mDbHelper: DataBaseHelper?=null

    var ITEM_TEXT =  ""
    var item_current:Item?=null

    const val APP_JSON_FILE_NAME = "app_data_full.json"
//    const val ASSET_URI = "file:///android_asset/app_images/"
//    const val ASSET_URI = "file:///android_asset/"
const val ASSET_URI = "asset://android_asset/" //Fresco Asset URI

}