package com.facebook.galleryapp

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.view.Menu
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.facebook.galleryapp.AdObject.AD_DISPLAY_SCROLL_COUNT
import com.facebook.galleryapp.AdObject.admob
import java.io.IOException

class ItemFragment02 : androidx.fragment.app.Fragment() {
    private var itemLayout: View? = null
    private var imgBitmap: Bitmap? = null
    private var mImages: ArrayList<String>? = null

    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var vpItemImage: ViewPager
    private var vpAdapter: androidx.viewpager.widget.PagerAdapter? = null
    private var mAdCount = 0
    private lateinit var appInterfaces: AppInterfaces
    private lateinit var mLayoutInflater: LayoutInflater
    private lateinit var vpItemImageView: ImageView

    init {
        mImages=getCurrentItemMenus()
        vpAdapter = ImagePagerAdapter()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AppInterfaces) {
            appInterfaces = activity as AppInterfaces
        }

    }

   /* override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is AppInterfaces) {
            appInterfaces = activity
        }
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutInit(inflater, container)
        setupToolbarActions()
        loadViewPager()
        setupToolbarTitle()
        setupViewPagerListener()
        return itemLayout

    }

    private fun layoutInit(inflater: LayoutInflater, container: ViewGroup?) {
        mLayoutInflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        itemLayout = inflater.inflate(R.layout.fragment_item, container, false)
        vpItemImage = itemLayout?.findViewById<ViewPager>(R.id.vpItemImage)!!
    }

    private fun setupToolbarActions() {
        val act = activity as AppCompatActivity
        itemLayout?.let {

            toolbar = it.findViewById(R.id.my_toolbar)
                act.setSupportActionBar(toolbar)
        }
        setHasOptionsMenu(true)
    }

    private fun loadViewPager() {
        vpItemImage.adapter = vpAdapter
        vpItemImage.currentItem = ItemDataset.position
    }
    private fun setupToolbarTitle() {
        ItemDataset.position_bookmark = getCurrentItemFromVP() //Save the position of the current item
        toolbar.title = "${getItemPositionFromView() + 1}/${getCurrentItemPosition()}"
    }
    private fun getItemPositionFromView(): Int {
//        return itemLayout?.vpItemImage?.currentItem ?:0
        return vpItemImage?.currentItem ?:0
    }
    private fun getCurrentItemPosition(): Int {
        return getCurrentItemMenus()?.size ?:0
    }
    private fun getCurrentItemMenus(): ArrayList<String> {
        return ItemDataset?.item_current?.menus ?: arrayListOf()
    }
    private fun setupViewPagerListener() {
        vpItemImage?.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {

            override fun onPageSelected(position: Int) {
                loadNextItemWithAds(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                setupToolbarTitle()
            }
        })
    }


    private fun loadNextItemWithAds(position: Int) {
        mAdCount++
        if (mAdCount % AD_DISPLAY_SCROLL_COUNT == 0) admob?.loadNextScreen { }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)

        setHasOptionsMenu(true)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

         imgBitmap = getBitmapImage()
        when (item.itemId) {
            R.id.btnWallpaper -> {
                setupWallpaper()
            }
            R.id.btnShare -> {
                shareImage()
            }
            R.id.btnBookmark -> {
                ItemDataset.mDbHelper?.addBookMark( imgName = getCurrentItemFromDataset())
                Snackbar.make(requireActivity().findViewById(R.id.clMainActivity), "Image ${getCurrentItemFromDataset()} saved successfully.", Snackbar.LENGTH_SHORT).show()

            }
            R.id.btnBookMarksList -> {
                loadBookMarkListScreen()
            }
            R.id.btnRate -> {
                AppUtils().rateApp(activity as Context)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun getBitmapImage(): Bitmap {
        var img:Bitmap?=null
        try {
            val uri = getCurrentItemFromDataset()
            if (uri.isNotBlank()) {
                img = BitmapFactory.decodeStream(activity?.assets?.open(getCurrentItemFromDataset()))
            }
        }catch (e:Exception){
            img=getDummyImage()
        }
        return img?:getDummyImage()
    }

    private fun setupWallpaper() {
        val myWallpaperManager = WallpaperManager.getInstance(context)
        try {
            myWallpaperManager.clear()
            imgBitmap.let { myWallpaperManager.setBitmap(it) }
            activity?.let { AppUtils().showSneakerMsg(it,"Wallpaper Set Successfully.") }

        } catch (e: IOException) {
            activity?.let { AppUtils().showSneakerMsg(it,"Unable to set Wallpaper!") }
        }
    }

    private fun shareImage() {
        imgBitmap?.let { AppUtils().shareImage(it, context as Context)}
    }

    private fun loadBookMarkListScreen() {
        admob?.loadNextScreen {
            appInterfaces.loadBookMarkMenu()
        }
    }

    private fun getSelectedItem(): Bitmap {
        var img:Bitmap?=null
        try {
            val uri = getCurrentItemFromDataset()
            if (uri.isNotBlank()) {
                img = BitmapFactory.decodeStream(activity?.assets?.open(getCurrentItemFromDataset()))
            }
        }catch (e:Exception){
            img=getDummyImage()
        }
        return img ?:getDummyImage()
    }
    private fun getDummyImage(): Bitmap {
        return BitmapFactory.decodeResource(context?.resources, R.drawable.gallery)
    }
    private fun getCurrentItemFromDataset(): String {
        return getCurrentItemMenus()[getCurrentItemFromVP()] ?:""
    }
    private fun getCurrentItemFromVP(): Int {
        return vpItemImage?.currentItem ?:0
    }



    private inner class ImagePagerAdapter :
        androidx.viewpager.widget.PagerAdapter() {
        private val mImages = getCurrentItemMenus()

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun getCount(): Int {
            return mImages?.size ?: 0
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object` as LinearLayout
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false)
            val iconPath = getIconPath(position)
            loadImageIntoView(itemView, iconPath, container)
            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as LinearLayout)
        }
    }

    private fun loadImageIntoView(
        itemView: View,
        iconPath: String,
        container: ViewGroup
    ) {
        val imgHolder = itemView.findViewById<ImageView>(R.id.suit_image)
        imgHolder.setImageURI(iconPath.toUri())
        AppUtils().setupPinchZoom( itemView,iconPath,container) /*New PinchZoom*/
    }

    private fun getIconPath(position: Int): String {
        var iconPath=""
     try {
         iconPath= ItemDataset.ASSET_URI + (mImages?.elementAt(position) ?: 0)
     }catch (ex:IndexOutOfBoundsException){
         appInterfaces.loadMenus()
     }
        return iconPath
    }


}
