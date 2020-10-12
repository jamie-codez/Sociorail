package com.voomantics.sociorail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class AccountActivity : AppCompatActivity() {
    lateinit var image: ImageView
    lateinit var nameTV: TextView
    lateinit var emailTV: TextView
    lateinit var phoneTV: TextView
    lateinit var sexTV: TextView
    private lateinit var nameET: EditText
    private lateinit var emailET: EditText
    private lateinit var phoneET: EditText
    private lateinit var sexSp: Spinner
    private lateinit var edit: Button
    private lateinit var save: Button
    lateinit var name: String
    lateinit var email: String
    lateinit var phone: String
    lateinit var sex: String
    lateinit var fUser: FirebaseUser
    private lateinit var selectPicUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        image = findViewById(R.id.pro_pic)
        nameTV = findViewById(R.id.nameTV)
        nameET = findViewById(R.id.nameET)
        emailTV = findViewById(R.id.emailTV)
        emailET = findViewById(R.id.emailET)
        phoneTV = findViewById(R.id.phoneTV)
        phoneET = findViewById(R.id.phoneET)
        sexTV = findViewById(R.id.sex)
        sexSp = findViewById(R.id.genderSpinner)
        edit = findViewById(R.id.editButton)
        save = findViewById(R.id.saveButton)

        getInfo()
        edit.setOnClickListener {
            nameTV.visibility = View.GONE
            emailTV.visibility = View.GONE
            phoneTV.visibility = View.GONE
            sexTV.visibility = View.GONE
            edit.isEnabled = false
            nameET.visibility = View.VISIBLE
            emailET.visibility = View.VISIBLE
            phoneET.visibility = View.VISIBLE
            sexTV.visibility = View.VISIBLE
            sexSp.visibility = View.VISIBLE
            image.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
        save.setOnClickListener {
            addImageToDatabase()
        }
    }

    private fun getInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val user = i.value as User
                    if (user.uid == fUser.uid) {
                        email = user.email.toString()
                        name = user.name.toString()
                        phone = user.phone.toString()
                        sex = user.sex.toString()
                        nameTV.text = user.name
                        emailTV.text = user.email
                        phoneTV.text = user.phone
                        sexTV.text = user.sex
                        Picasso.get().load(user.imageUrl).centerInside().resize(150, 150).placeholder(R.drawable.account).into(image)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun addImageToDatabase() {
        name = nameET.text.toString().trim { it <= ' ' }
        sex = sexSp.selectedItem.toString()
        val filename = UUID.randomUUID()
        val storage = FirebaseStorage.getInstance().getReference("image/$filename")
        storage.putFile(selectPicUri).addOnCompleteListener {
            Log.i("Sign up", "Successfully uploaded image to database")
            storage.downloadUrl.addOnSuccessListener { uri: Uri -> addUserToDB(uri.toString()) }
        }
    }

    private fun addUserToDB(uri: String?) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("users")
        val user = User(uid, name, email, phone, sex, uri)
        reference.child(uid).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_LONG).show()
            nameTV.visibility = View.VISIBLE
            emailTV.visibility = View.VISIBLE
            phoneTV.visibility = View.VISIBLE
            sexTV.visibility = View.VISIBLE
            edit.isEnabled = true
            nameET.visibility = View.GONE
            emailET.visibility = View.GONE
            phoneET.visibility = View.GONE
            sexTV.visibility = View.GONE
            sexSp.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && requestCode == Activity.RESULT_OK && data != null) {
            selectPicUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectPicUri)
            image.setImageBitmap(bitmap)
            image.setAlpha(0)
        }
    }
}