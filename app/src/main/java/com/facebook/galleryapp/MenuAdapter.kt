package com.facebook.galleryapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter(val ctx: Context, val act: FragmentActivity?, val appInterfaces: AppInterfaces) :
    RecyclerView.Adapter<MenuAdapter.ViewHolder>() {


    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v = LayoutInflater.from(ctx).inflate(R.layout.menu_layout, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return ItemDataset.item_current?.menus?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val imgPath = AppUtils().getMenuImagePath(position).toString()
        val imgHolder = holder.v.findViewById<ImageView>(R.id.imgMenuIcon)
        imgHolder.setImageURI(imgPath.toUri())
        holder.v.setOnClickListener {

            ItemDataset.position = position


            if (act is AppInterfaces) {
                AdObject.admob?.loadNextScreen { appInterfaces.loadItem() }
            }


        }

    }


}