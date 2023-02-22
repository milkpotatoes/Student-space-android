package com.milkpotatoes.studentspace

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.milkpotatoes.studentspace.R
import android.content.Intent
import android.widget.Toast

class LoginFromWebActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_from_web)
        // ATTENTION: This was auto-generated to handle app links.
        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val startIntent = Intent(this@LoginFromWebActivity, MainActivity::class.java)
        startIntent.data = appLinkIntent.data
//        Toast.makeText(this, appLinkIntent.data.toString(), Toast.LENGTH_SHORT).show()
        startActivity(startIntent)
        finish()
    }
}