package com.facebook.galleryapp


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class BookmarkFragment : androidx.fragment.app.Fragment() {
    private lateinit var appInterfaces:AppInterfaces

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AppInterfaces){ appInterfaces = activity as AppInterfaces
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the rate_me_layout for this fragment
        val v = inflater.inflate(R.layout.fragment_bookmark, container, false)
        v.findViewById<TextView>(R.id.tvBookMarkTitle).text="BookMarked Items"
        v.findViewById<RecyclerView>(R.id.rvBookMarks).apply {
            setHasFixedSize(true)
//            layoutManager = GridAutofitLayoutManager(activity as Context, AdObject.GRID_IMAGE_WIDTH, LinearLayoutManager.VERTICAL, false)
            layoutManager = GridLayoutManager(requireActivity(),2)
            adapter = BookMarkAdapter(context,requireActivity() ,appInterfaces)

        }
        return  v
    }



}
