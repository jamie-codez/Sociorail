package com.voomantics.sociorail

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class PairOption : AppCompatActivity() {
    private lateinit var request: Request
    lateinit var time: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_option)
        getData()
        val name: TextView = findViewById(R.id.name)
        val jigsaw: ImageView = findViewById(R.id.jigsaw)
        val accept: Button = findViewById(R.id.accept)
        val reject: Button = findViewById(R.id.reject)
        time = findViewById(R.id.timer)
        name.text = request.fromName

        val timer = object : CountDownTimer(300000, 1000) {
            override fun onTick(p0: Long) {
                val seconds = p0 / 1000
                time.text = String.format("%s : %s", seconds / 60, seconds % 60)
                if ((p0 / 10000) % 60 <= 60) {
                    time.setTextColor(Color.RED)
                }
            }

            override fun onFinish() {
                Toast.makeText(this@PairOption, "Time is up...", Toast.LENGTH_LONG).show()
                accept.isEnabled = false
                reject.isEnabled = false
            }
        }
        timer.start()

        accept.setOnClickListener {
            jigsaw.setImageResource(R.drawable.jigsaw2)
            sendResponse("Accepted")
        }
        reject.setOnClickListener {
            jigsaw.setImageResource(R.drawable.rejected)
            sendResponse("Rejected")
        }
    }

    private fun getData() {
        val intent = intent
        if (intent.hasExtra("Request")) {
            request = intent.getParcelableExtra("Request")!!
        }
        intent.removeExtra("Request")
    }

    private fun sendResponse(response: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Requests").child(request.to!!).child(request.from!!)
        val map = HashMap<String, String>()
        map["response"] = response
        ref.updateChildren(map as Map<String, Any>).addOnSuccessListener { Toast.makeText(this, "Response sent...", Toast.LENGTH_LONG).show() }
    }

}