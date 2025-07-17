package com.facebook.galleryapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

class BookMarkAdapter(private val ctx:Context, private val act: FragmentActivity?, val appInterfaces: AppInterfaces): RecyclerView.Adapter<BookMarkAdapter.ViewHolder>() {


    //GET THE LIST OF IMAGES IN THE BOOKMARK FOLDER
//    val images_bookmarked = ItemDataset.APP_DIR.listFiles()
    private var imagesBookmarked:ArrayList<String>? = null

    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

    init {
        imagesBookmarked = ItemDataset.mDbHelper?.getAllBookMarks()?: arrayListOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        val v = LayoutInflater.from(ctx).inflate(R.layout.menu_layout,parent,false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
         return imagesBookmarked?.size?:0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val imgPath = AppUtils().getBookmarkImagePath(imagesBookmarked,position).toString()
        val imgHolder = holder.v.findViewById<ImageView>(R.id.imgMenuIcon)
        imgHolder.setImageURI(imgPath.toUri())
        holder.v.setOnClickListener {
    //GET THE IMAGE INDEX BASED ON THE IMAGE NAME
            ItemDataset.position_bookmark = position

            AdObject.admob?.loadNextScreen {  appInterfaces.loadBookMarkItem() }


        }

    }
}