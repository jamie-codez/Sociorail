package com.voomantics.sociorail

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RedeemTokenActivity : AppCompatActivity() {
    lateinit var request: Request
    private lateinit var myName: TextView
    private lateinit var receiptNo: EditText
    private lateinit var partnerName: TextView
    lateinit var fUser: FirebaseUser
    lateinit var tokenNo: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem_token)
        fUser = FirebaseAuth.getInstance().currentUser!!
        tokenNo = findViewById(R.id.token_no)
        myName = findViewById(R.id.my_name)
        receiptNo = findViewById(R.id.receipt_number)
        partnerName = findViewById(R.id.partner_name)
        readToken()
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
            request = intent.getParcelableExtra("Request")!!
            myName.text = request.fromName
            partnerName.text = request.toName
        }
    }

    private fun readToken() {
        val ref = FirebaseDatabase.getInstance().getReference("OfferTokens").child(request.to!!).child(request.from!!)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val mToken = i.value as Token
                    if (mToken.from == fUser.uid && mToken.to == request.to && mToken.status == "unredeemed") {
                        tokenNo.text = mToken.token
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun redeemToken() {
        val mReceiptNo = receiptNo.text.toString().trim()
        if (TextUtils.isEmpty(mReceiptNo)){
            Toast.makeText(this@RedeemTokenActivity,"Enter receipt number",Toast.LENGTH_LONG).show()
        }else{
            saveReceipt(mReceiptNo)
            val ref = FirebaseDatabase.getInstance().getReference("OfferTokens").child(request.to!!).child(request.from!!)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val mToken = i.getValue(Token::class.java)
                        if (mToken!!.from == fUser.uid && mToken.to == request.to && mToken.status == "unredeemed") {
                            val map = HashMap<String, String>()
                            map["status"] = "redeemed"
                            ref.updateChildren(map as Map<String, Any>).addOnSuccessListener {
                                Toast.makeText(this@RedeemTokenActivity,"Token Redeemed",Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            Toast.makeText(this, "Token redeemed", Toast.LENGTH_LONG).show()
            finish()
        }

    }
    private fun saveReceipt(receiptNo:String){
        val ref = FirebaseDatabase.getInstance().getReference("Receipt_numbers")
        ref.setValue(receiptNo).addOnSuccessListener {
            Log.i("RedeemToken","Receipt number saved")
        }
    }
}