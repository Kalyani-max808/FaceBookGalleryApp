package com.facebook.galleryapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.facebook.galleryapp.ItemDataset.ASSET_URI
import com.facebook.galleryapp.ItemDataset.item_current
import me.relex.photodraweeview.PhotoDraweeView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val LOG_TAG = "GALLERY_APP"
class AppUtils {
//    private val logger = KotlinLogging.logger {}

    init {
    }
    fun shareImage(bmp: Bitmap,ctx:Context) {

        val share = Intent(Intent.ACTION_SEND)
        share.putExtra(Intent.EXTRA_TEXT, "Please Download the app")
        share.putExtra(Intent.EXTRA_TEXT, "Hey please check this application \n https://play.google.com/store/apps/details?id="+AdObject.PACKAGE_NAME)
        share.type = "image/*"

        val bytes = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        val f = File(ItemDataset.APP_DIR,"temp.jpg")
        val uriShare = FileProvider.getUriForFile(ctx,
            AdObject.PACKAGE_NAME + ".fileprovider", f)

        try {
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        share.putExtra(Intent.EXTRA_STREAM, uriShare)
        try {
            ctx.startActivity(Intent.createChooser(share, "Share via"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(ctx, "Please Connect To Internet", Toast.LENGTH_LONG)
                .show()
        }

    }

    fun loadGalleryFromAssets(context: Context){
        ItemDataset.topics_icons= getGalleryIconListwithPath(context.assets.list("gallery_icons"))
        val galleryDirectoryList=context.assets.list("app_images")
        val assets_exception = context.getString(R.string.assets_exception)
        validateGalleryIcons(ItemDataset.topics_icons,galleryDirectoryList,assets_exception)
        ItemDataset.items = ArrayList()
        galleryDirectoryList.let {
            it?.forEachIndexed { index, it ->
                ItemDataset.items.add(
                    Item(topic_title = it,
                        topic_icon = ItemDataset.topics_icons[index],
                        menus = getImageListFromGalleryDirectory(context, it)
                    ))
            }

        }}

    private fun validateGalleryIcons(
        topicsIcons: ArrayList<String>,
        galleryDirectoryList: Array<String>?,
        assets_exception: String
    ) {
        if (galleryDirectoryList != null) {
            if (topicsIcons.size<galleryDirectoryList.size){
                showSnackbarMsg(assets_exception)
                logErrorMsg(assets_exception)
            }
        }
    }

    private fun getGalleryIconListwithPath(galleryIconList: Array<String>?): ArrayList<String> {
        val iconsList:ArrayList<String> = ArrayList()
        galleryIconList!!.forEach { iconsList.add("gallery_icons/$it") }
        return iconsList
    }

    private fun getImageListFromGalleryDirectory(context: Context, galleryDirectory: String?):ArrayList<String> {
        val imagesList:ArrayList<String> = ArrayList()
        val galleryDirectoryList=context.assets.list("app_images/$galleryDirectory")?.toCollection(ArrayList<String>())?: arrayListOf()
        galleryDirectoryList.forEach { image ->
            if (image.isNotBlank())    imagesList.add("app_images/$galleryDirectory/$image")
        }
/*
        context.assets.list("app_images/$galleryDirectory")!!.toCollection(ArrayList<String>()).forEach {
            imagesList.add("app_images/$galleryDirectory/$it")
        }
*/
        return imagesList
    }


    private fun streamingArray(array: String): Collection<Item> {


        val gson = Gson()

        // Deserialization
        val collectionType = object : TypeToken<Collection<Item>>() {}.type


//
//        }
        return gson.fromJson(array, collectionType)
    }

    fun showSnackbarMsg( msgText: String) {
        AdObject.snackbarContainer?.let {
            Snackbar.make(
                it,
                msgText,
                Snackbar.LENGTH_SHORT
            ).show()
        }


    }
    fun showSneakerMsg(target:Activity,msgText:String){
        showSnackbarMsg(msgText)
/*
        AdObject.snackbarContainer.let {
            Sneaker.with(target) // Activity, Fragment or ViewGroup
//                .setTitle("Success!!")
                .setMessage(msgText)
                .sneakSuccess()
        }
*/
    }
    fun showToastMsg(){}
    fun logErrorMsg(errMsg:String){
        Log.e(LOG_TAG,errMsg)
    }
    fun logDebugMsg(msgText: String){
        Log.d(LOG_TAG,msgText)
    }
    fun logInfoMsg(msgText: String){
        Log.i(LOG_TAG,msgText)
    }

    fun rateApp(ctx: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ctx.packageName))
            var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            flags = if (Build.VERSION.SDK_INT >= 21) {
                flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
            } else {

                flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
            }
            intent.addFlags(flags)
            ContextCompat.startActivity(ctx, intent, null)
        } catch (e: ActivityNotFoundException) {
            AppUtils().showSnackbarMsg("You don't have any app that can open this link")
//            Toast.makeText(ctx, "You don't have any app that can open this link", Toast.LENGTH_SHORT).show()
        }
    }
    fun shareApp(context: Context) {
        val appPackageName = context.packageName
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Check out the App at: https://play.google.com/store/apps/details?id=$appPackageName"
        )
        sendIntent.type = "text/plain"
        context.startActivity(sendIntent)
    }
    /*--------------------------------TOPIC IMAGE-----------------------------*/
    fun getTopicImagePath(position: Int): Uri =
        Uri.parse(ASSET_URI + getCurrentTopicURI(position))
    private fun getCurrentTopicURI(position: Int): String {
        Uri.parse(ItemDataset.ASSET_URI + ItemDataset.topics_icons[position])
        var result: String
        try {
            result = (ItemDataset.topics_icons[position]) ?: AdObject.DUMMY_IMAGE_URI
        } catch (ex: Exception) {
            result = AdObject.DUMMY_IMAGE_URI
        }
        return result
    }
    /*--------------------------------MENU IMAGE-----------------------------*/
    fun getMenuImagePath(position: Int): Uri =
        Uri.parse(ASSET_URI + getCurrentMenuImageURI(position))
    private fun getCurrentMenuImageURI(position: Int): String {
        var result: String
        try {
            result = (item_current?.menus?.get(position)) ?: AdObject.DUMMY_IMAGE_URI
        } catch (ex: Exception) {
            result = AdObject.DUMMY_IMAGE_URI
        }
        return result
    }
    /*--------------------------------BOOKMARK IMAGE-----------------------------*/
    fun getBookmarkImagePath(imagesBookmarked: ArrayList<String>?, position: Int): Uri =
        Uri.parse(ASSET_URI + getCurrentBookmarkImageURI(imagesBookmarked,position))
    private fun getCurrentBookmarkImageURI(imagesBookmarked: ArrayList<String>?, position: Int): String {
        var result: String
        try {
            result = (imagesBookmarked?.elementAt(position)) ?: AdObject.DUMMY_IMAGE_URI
        } catch (ex: Exception) {
            result = AdObject.DUMMY_IMAGE_URI
        }
        return result
    }
    /*--------------------------------ITEM IMAGE-----------------------------*/
    fun getItemImagePath(itemImages: ArrayList<String>?, position: Int): Uri =
        Uri.parse(ASSET_URI + getCurrentItemImageURI(itemImages,position))
    private fun getCurrentItemImageURI(imagesBookmarked: ArrayList<String>?, position: Int): String {
        var result: String
        try {
            result = (imagesBookmarked?.elementAt(position)) ?: AdObject.DUMMY_IMAGE_URI
        } catch (ex: Exception) {
            result = AdObject.DUMMY_IMAGE_URI
        }
        return result
    }
    /*--------------------------------LOAD IMAGE--------------------------------*/
    fun loadImageIntoViewWithFresco(
        imgHolder: SimpleDraweeView,
        imgPath: String,
    ) {
        imgHolder.setImageURI(imgPath)    }
    /*------------------------------PICH ZOOM----------------------------------*/
    fun setupPinchZoom(itemView: View, iconPath: String, container: ViewGroup) {
        val photoDraweeView:PhotoDraweeView = itemView.findViewById(R.id.suit_image)
        val controller = Fresco.newDraweeControllerBuilder()
        controller.setUri(iconPath)


        controller.oldController = photoDraweeView.controller
        controller.controllerListener = object : BaseControllerListener<ImageInfo?>() {
            override fun onFinalImageSet(
                id: String,
                imageInfo: ImageInfo?,
                animatable: Animatable?
            ) {
                super.onFinalImageSet(id, imageInfo, animatable)
                if (imageInfo == null) {
                    return
                }
                photoDraweeView.update(imageInfo.width, imageInfo.height)
            }
        }
        photoDraweeView.controller = controller.build()

        addItemImageToViewPager(container, itemView)

    }

    private fun addItemImageToViewPager(
        container: ViewGroup,
        itemView: View
    ) {
        (container as ViewPager).addView(
            itemView, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}



