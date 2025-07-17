package com.facebook.galleryapp


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TopicFragment : androidx.fragment.app.Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var appInterfaces:AppInterfaces
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is AppInterfaces){ appInterfaces = activity }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the rate_me_layout for this fragment
        val v:View = inflater.inflate(R.layout.fragment_topics, container, false)
        val rvJokeHeader = v.findViewById<RecyclerView>(R.id.rvJokeHeader)
        //RecyclerView Logic
//        v.tvHeader.text = "${ItemDataset.items.size} Categories loaded.."

        rvJokeHeader.apply {
//            layoutManager = GridAutofitLayoutManager(activity as Context, AdObject.GRID_IMAGE_WIDTH, LinearLayoutManager.VERTICAL, false)
            layoutManager = GridLayoutManager(requireContext(),1)
            adapter = TopicAdapter(context, activity,appInterfaces)
            setHasFixedSize(true)
            setItemViewCacheSize(10)
            //            layoutManager = LinearLayoutManager(activity as Context)

//            addItemDecoration(BoundaryItemDecoration(context,Color.BLUE,5))
//            addItemDecoration(CustomItemDecoration(spacing = 10,includeEdge = false))
        }

//        /*Change the number of columns based on screen orientation*/
//        if (activity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            rv.layoutManager=GridAutofitLayoutManager(activity as Context,500,LinearLayoutManager.VERTICAL,false)
//        } else {
//            rv.layoutManager=GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
////            rv.layoutManager=GridAutofitLayoutManager(activity as Context,500,LinearLayoutManager.HORIZONTAL,false)
//        }
        return v
    }
}
