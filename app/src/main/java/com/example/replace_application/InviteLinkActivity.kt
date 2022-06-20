package com.example.replace_application

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.replace_application.databinding.ActivityInviteLinkBinding
import com.example.replace_application.model.UserModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class InviteLinkActivity : AppCompatActivity() {

    private val TAG = InviteLinkActivity::class.java.simpleName
    private lateinit var binding : ActivityInviteLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invite_link)

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

                if(deepLink != null) {
                    handleDynamicLink(deepLink)
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

    }
    private fun handleDynamicLink(deepLink: Uri) {

        if(deepLink.path == "/join_couple") {
            var code = deepLink.getQueryParameter("code")
            if(FBAuth.getUid() != null) {
                val postListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val dataModel = dataSnapshot.getValue(UserModel::class.java)

                        if(dataModel!!.coupleConnect == true) {
                            Toast.makeText(baseContext, "이미 연결이 되어있습니다.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            binding.okBtn.setOnClickListener {
                                FBRef.myUserRef
                                    .child(FBAuth.getUid())
                                    .push()
                                    .child("coupleConnect")
                                    .setValue(true)

                                FBRef.myUserRef
                                    .child(code!!)
                                    .push()
                                    .child("coupleConnect")
                                    .setValue(true)

                                val intent = Intent(baseContext, MainActivity::class.java)
                                startActivity(intent)
                            }
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