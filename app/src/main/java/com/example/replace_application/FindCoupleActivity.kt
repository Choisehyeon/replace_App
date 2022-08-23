package com.example.replace_application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.replace_application.database.UserDatabase

import com.example.replace_application.databinding.ActivityFindCoupleBinding
import com.example.replace_application.model.UserModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.template.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FindCoupleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindCoupleBinding
    private val TAG = FindCoupleActivity::class.java.simpleName
    var code: String? = ""
    private lateinit var db : UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_couple)

        db = UserDatabase.getDatabase(this)

        /*FBRef.myUserRef.child(FBAuth.getUid()).child("inviteCode").get().addOnSuccessListener {
            Log.d("Code", it.value.toString())
            binding.codeArea.text = it.value.toString()
            code = it.value.toString()
            Log.d(TAG, it.value.toString())
        }*/

        CoroutineScope(IO).launch {
            code = db.userDao().getInviteCode(FBAuth.getUid())
            binding.codeArea.text = code
        }



        binding.after.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.shareCodeBtn.setOnClickListener {
            val link = "https://replaceapp.page.link/join_couple?code=$code"
            var template: FeedTemplate? = null

            val dynamicLink =
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(link))
                    .setDomainUriPrefix("https://replaceapp.page.link")
                    .setAndroidParameters(
                        DynamicLink.AndroidParameters.Builder().build()
                    ).buildShortDynamicLink()
                    .addOnSuccessListener { (shortLink, flowChartLink) ->
                        Log.d(TAG, shortLink.toString())
                        template = getTemplate(code!!, shortLink!!)

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

        binding.enterCodeBtn.setOnClickListener {
            val intent = Intent(this, TypeLinkActivity::class.java)
            startActivity(intent)
        }

        checkInviteLink()
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

    private fun checkInviteLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                if (deepLink != null) {
                    handleDynamicLink(deepLink)
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

    }

    private fun handleDynamicLink(deepLink: Uri) {

        if (deepLink.path == "/join_couple") {
            var code = deepLink.getQueryParameter("code")
            if (FBAuth.getUid() != null) {
                val postListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val dataModel = dataSnapshot.getValue(UserModel::class.java)

                        if (!dataModel!!.coupleId.equals("")) {
                            Toast.makeText(baseContext, "이미 연결이 되어있습니다.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {

                            val intent = Intent(baseContext, InviteLinkActivity::class.java)
                            startActivity(intent)

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    }
                }
                FBRef.myUserRef.child(FBAuth.getUid()).addValueEventListener(postListener)

            } else {
                val intent = Intent(this, JoinActivity::class.java)
                intent.putExtra("code", code)
                startActivity(intent)
            }
        }
    }
}