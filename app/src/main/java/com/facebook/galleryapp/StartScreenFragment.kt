package com.facebook.galleryapp


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class StartScreenFragment : androidx.fragment.app.Fragment() {
    lateinit var act:AppInterfaces

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the rate_me_layout for this fragment
//        val v = inflater.inflate(R.layout.fragment_start_screen, container, false)
        val startScreen = inflater.inflate(R.layout.fragment_start_screen, container, false)
        val btnGallery03 = startScreen.findViewById<View>(R.id.btnGallery03)
        val btnGalleryLayout03 = startScreen.findViewById<View>(R.id.btnGalleryLayout03)
        val imgBtnRateMe03 = startScreen.findViewById<View>(R.id.imgBtnRateMe03)
        val imgBtnShare03 = startScreen.findViewById<View>(R.id.imgBtnShare03)
        val tvPrivacy03 = startScreen.findViewById<View>(R.id.tvPrivacy03)

        btnGalleryLayout03.setOnClickListener {
            AdObject.admob?.loadNextScreen { act.loadImageTopics() }
        }
        btnGallery03.setOnClickListener {
            AdObject.admob?.loadNextScreen { act.loadImageTopics() }
        }
        imgBtnRateMe03.setOnClickListener {
            AppUtils().rateApp(context as Context)
        }
        imgBtnShare03.setOnClickListener(View.OnClickListener {
            AppUtils().shareApp(context as Context)
        })

        tvPrivacy03.setOnClickListener {
            AdObject.admob?.loadNextScreen{act.loadPrivacyPolicy()}
        }


        return startScreen
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppInterfaces){
            act = context
        }
    }
}
