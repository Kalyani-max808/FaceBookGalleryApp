package com.facebook.galleryapp


import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jaredrummler.html.*
import java.io.IOException


class PrivacyPolicyFragment : androidx.fragment.app.Fragment() {
    private lateinit var appInterfaces:AppInterfaces
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is AppInterfaces){ appInterfaces = activity }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the rate_me_layout for this fragment
        val v = inflater.inflate(R.layout.fragment_privacy, container, false)
//        loadPrivacyPolicyText(v.tvPVtextView)
        setupPrivacyPolicy(v)
        return v
    }
    //https://github.com/jaredrummler/HtmlDsl
    private fun setupPrivacyPolicy(v: View) {
        v.findViewById<TextView>(R.id.tvPVtextView).setHtml {
             div(align = Attribute.Align.CENTER,{ h1("Privacy Policy").br() })
            p("Mindgame built the MindGameApps app as a Free app. This SERVICE is provided by Mindgame at no cost and is intended for use as is.").br()
            p("This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service.").br()
            p("If you choose to use my Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that I collect is used for providing and improving the Service. I will not use or share your information with anyone except as described in this Privacy Policy.").br()
            p("The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at MindGameApps unless otherwise defined in this Privacy Policy.").br()
            h2("Information Collection and Use:").br()
            p("For a better experience, while using our Service, I may require you to provide us with certain personally identifiable information, including but not limited to . The information that I request will be retained on your device and is not collected by me in any way.").br()
            p("The app does use third party services that may collect information used to identify you.").br()
            p("Reference to third party service providers used by the app are below:").br()
            ul{
                li("Admob.")
                li("Admob.")
                li("Google Analytics.")
            }.br()
            h2("Log Data:").br()
            p("I want to inform you that whenever you use my Service, in a case of an error in the app I collect data and information (through third party products) on your phone called Log Data. This Log Data may include information such as your device Internet Protocol (“IP”) address, device name, operating system version, the configuration of the app when utilizing my Service, the time and date of your use of the Service, and other statistics.").br()
            h2("Cookies:").br()
            p("").br()
            p("Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device's internal memory.").br()
            p("This Service does not use these “cookies” explicitly. However, the app may use third party code and libraries that use “cookies” to collect information and improve their services. You have the option to either accept or refuse these cookies and know when a cookie is being sent to your device. If you choose to refuse our cookies, you may not be able to use some portions of this Service.").br()
            h2("Service Providers:").br()
            p("I may employ third-party companies and individuals due to the following reasons:").br()
            p("To facilitate our Service;\n" +
                    "To provide the Service on our behalf;\n" +
                    "To perform Service-related services; or\n" +
                    "To assist us in analyzing how our Service is used.").br()
            p("I want to inform users of this Service that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.").br()

            h2("Security:").br()
            p("I value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and I cannot guarantee its absolute security.").br()
            h2("Links to Other Sites:").br()
            p("This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by me. Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.").br()
            h2("Children’s Privacy:").br()
            p("These Services do not address anyone under the age of 13. I do not knowingly collect personally identifiable information from children under 13. In a case I discover that a child under 13 has provided me with personal information, I immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact me so that I will be able to take necessary actions.").br()
            h2("Changes to This Privacy Policy:").br()
            p("I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page.").br()
            p("This policy is effective as of 14-10-2021").br()

            h1("Terms of Use")
            p("By downloading or using the app, these terms will automatically apply to you – you should make sure therefore that you read them carefully before using the app. You’re not allowed to copy, or modify the app, any part of the app, or our trademarks in any way. You’re not allowed to attempt to extract the source code of the app, and you also shouldn’t try to translate the app into other languages, or make derivative versions. The app itself, and all the trade marks, copyright, database rights and other intellectual property rights related to it, still belong to Mindgame.").br()
            p("Mindgame is committed to ensuring that the app is as useful and efficient as possible. For that reason, we reserve the right to make changes to the app or to charge for its services, at any time and for any reason. We will never charge you for the app or its services without making it very clear to you exactly what you’re paying for.").br()
            p("The MindGameApps app stores and processes personal data that you have provided to us, in order to provide my Service. It’s your responsibility to keep your phone and access to the app secure. We therefore recommend that you do not jailbreak or root your phone, which is the process of removing software restrictions and limitations imposed by the official operating system of your device. It could make your phone vulnerable to malware/viruses/malicious programs, compromise your phone’s security features and it could mean that the miz app won’t work properly or at all.").br()
            p("The app does use third party services that declare their own Terms and Conditions.").br()
            p("Terms and Conditions of third party service providers used by the app are bound to owners.").br()
            p(" Admob.  Google Play Services.  Google Analytics.              ").br()
            p("You should be aware that there are certain things that Mindgame will not take responsibility for. Certain functions of the app will require the app to have an active internet connection. The connection can be Wi-Fi, or provided by your mobile network provider, but Mindgame cannot take responsibility for the app not working at full functionality if you don’t have access to Wi-Fi, and you don’t have any of your data allowance left.").br()
            p("If you’re using the app outside of an area with Wi-Fi, you should remember that your terms of the agreement with your mobile network provider will still apply. As a result, you may be charged by your mobile provider for the cost of data for the duration of the connection while accessing the app, or other third party charges. In using the app, you’re accepting responsibility for any such charges, including roaming data charges if you use the app outside of your home territory (i.e. region or country) without turning off data roaming. If you are not the bill payer for the device on which you’re using the app, please be aware that we assume that you have received permission from the bill payer for using the app.").br()
            p("Along the same lines, Mindgame cannot always take responsibility for the way you use the app i.e. You need to make sure that your device stays charged – if it runs out of battery and you can’t turn it on to avail the Service, Mindgame cannot accept responsibility.").br()
            p("With respect to Mindgame’s responsibility for your use of the app, when you’re using the app, it’s important to bear in mind that although we endeavour to ensure that it is updated and correct at all times, we do rely on third parties to provide information to us so that we can make it available to you. Mindgame accepts no liability for any loss, direct or indirect, you experience as a result of relying wholly on this functionality of the app.").br()
            p("At some point, we may wish to update the app. The app is currently available on Android – the requirements for system(and for any additional systems we decide to extend the availability of the app to) may change, and you’ll need to download the updates if you want to keep using the app. Mindgame does not promise that it will always update the app so that it is relevant to you and/or works with the Android version that you have installed on your device. However, you promise to always accept updates to the application when offered to you, We may also wish to stop providing the app, and may terminate use of it at any time without giving notice of termination to you. Unless we tell you otherwise, upon any termination, (a) the rights and licenses granted to you in these terms will end; (b) you must stop using the app, and (if needed) delete it from your device.").br()
            h2("Changes to This Terms and Conditions:").br()
            p("I may update our Terms and Conditions from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Terms and Conditions on this page.").br()
            p("These terms and conditions are effective as of 14-10-2021.").br()
        }
    }
    private fun loadPrivacyPolicyText(tvPVtextView: TextView) {
        try {
            val privacy_policy_file_name = "privacy_policy.html"

            requireActivity().assets.open(privacy_policy_file_name).apply {
                val policyText = this.readBytes().toString(Charsets.UTF_8)
                tvPVtextView.htmlText(policyText)
            }.close()
        } catch (exception: IOException) {
            tvPVtextView.text = "Failed loading html."
            exception.printStackTrace()
        }
    }
    private fun TextView.htmlText(text: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
        } else {
            setText(Html.fromHtml(text))
        }
    }

}
