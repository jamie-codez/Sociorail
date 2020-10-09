package com.voomantics.sociorail

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RedeemTokenActivity : AppCompatActivity() {
    lateinit var request: Request
    lateinit var myName: TextView
    lateinit var partnerName: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem_token)
        val tokenNo: TextView = findViewById(R.id.token_no)
        myName = findViewById(R.id.my_name)
        partnerName = findViewById(R.id.partner_name)
        val buffer: ProgressBar = findViewById(R.id.buffer)
        val successBuff: ImageView = findViewById(R.id.click_redeem)
        getData()

        buffer.setOnClickListener {
            successBuff.visibility = View.VISIBLE
            buffer.visibility = View.INVISIBLE
            redeemToken()
        }
    }

    private fun getData() {
        val intent = intent
        if (intent.hasExtra("Request")) {
            request = intent.getParcelableExtra<Request>("Request")!!
            myName.text = request.fromName
            partnerName.text = request.toName
        }
    }
    private fun redeemToken(){
        Toast.makeText(this,"Token redeemed",Toast.LENGTH_LONG).show()
        finish()
    }
}