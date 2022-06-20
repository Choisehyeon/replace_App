package com.example.replace_application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import com.example.replace_application.databinding.ActivityFindCoupleBinding
import com.example.replace_application.utils.FBAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.template.model.*
import java.lang.Exception

class FindCoupleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindCoupleBinding
    private val TAG = FindCoupleActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_couple)

        binding.codeArea.text = FBAuth.getUid()

        val code = binding.codeArea.text.toString()
        Log.d(TAG, code)

        binding.shareCodeBtn.setOnClickListener {
            val link = "https://replace.page.link/join_couple?code=$code"
            var template: FeedTemplate? = null

            val dynamicLink =
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(link))
                    .setDomainUriPrefix("https://replace.page.link")
                    .setAndroidParameters(
                        DynamicLink.AndroidParameters.Builder().build()
                    ).buildShortDynamicLink()
                    .addOnSuccessListener { (shortLink, flowChartLink) ->
                        template = getTemplate(binding.codeArea.text.toString(), shortLink!!)

                        LinkClient.instance.defaultTemplate(this, template!!) { linkResult, error ->
                            if (error != null) {
                                Log.e(TAG, "카카오톡 공유 실패", error)
                            } else if (linkResult != null) {
                                Log.d(TAG, "카카오톡 공유 성공 ${linkResult.intent}")
                                startActivity(linkResult.intent)

                                // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                                Log.w(TAG, "Warning Msg: ${linkResult.warningMsg}")
                                Log.w(TAG, "Argument Msg: ${linkResult.argumentMsg}")
                            }
                        }
                    }
        }
    }


    private fun getTemplate(code: String, dynamicLink: Uri): FeedTemplate {

        val defaultFeed = FeedTemplate(
            content = Content(
                title = "초대 코드: ${code}",
                imageUrl = "https://postfiles.pstatic.net/MjAyMjA2MTRfMjY1/MDAxNjU1MjAyNDE1OTMw.lBHsPX8mjRhLAwFWOqgypjHPItmGqI1OtbRWeUOva-8g.BzUnrhSmj-lk9WO96Bs2qNe1_L5VoSvzLc4F3bxc-rQg.PNG.chltpgus6275/findcouple.png?type=w966",
                link = Link()
            ),
            buttons = listOf(
                Button(
                    "초대 수락하기",
                    Link(
                        webUrl = dynamicLink.toString(),
                        mobileWebUrl = dynamicLink.toString()
                    )
                )
            )
        )
        return defaultFeed
    }
}