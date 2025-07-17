package com.facebook.galleryapp


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MenuFragment : androidx.fragment.app.Fragment() {
    private lateinit var appInterfaces:AppInterfaces

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is AppInterfaces){ appInterfaces = activity }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the rate_me_layout for this fragment
        val v = inflater.inflate(R.layout.fragment_menu, container, false)
        val tvHeader = v.findViewById<TextView>(R.id.tvHeader)
        val rvMenuHeader = v.findViewById<RecyclerView>(R.id.rvMenuHeader)
        tvHeader.text = ItemDataset.item_current?.topic_title ?: ""
        rvMenuHeader.apply {
            setHasFixedSize(true)
//            layoutManager = GridAutofitLayoutManager(activity as Context, AdObject.GRID_IMAGE_WIDTH, LinearLayoutManager.VERTICAL, false)
            layoutManager = GridLayoutManager(activity,2)
            adapter = MenuAdapter(context,activity, appInterfaces)
            setHasFixedSize(true)
            setItemViewCacheSize(100)
        }
        return v
    }



}
