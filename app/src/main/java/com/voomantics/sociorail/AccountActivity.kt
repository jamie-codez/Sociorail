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

class AccountActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var image: ImageView
    lateinit var nameTV: TextView
    lateinit var emailTV: TextView
    lateinit var phoneTV: TextView
    private lateinit var genderTV: TextView
    lateinit var sexTV: TextView
    private lateinit var nameET: EditText
    private lateinit var emailET: EditText
    private lateinit var phoneET: EditText
    private lateinit var sexSp: Spinner
    private lateinit var edit: Button
    private lateinit var save: Button
    lateinit var name: String
    private lateinit var newName: String
    lateinit var email: String
    private lateinit var newEmail: String
    lateinit var phone: String
    private lateinit var newPhone: String
    lateinit var sex: String
    lateinit var imageUrl: String
    lateinit var fUser: FirebaseUser
    private var selectPicUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        image = findViewById(R.id.pro_pic)
        nameTV = findViewById(R.id.nameTV)
        nameET = findViewById(R.id.nameET)
        emailTV = findViewById(R.id.emailTV)
        emailET = findViewById(R.id.emailET)
        phoneTV = findViewById(R.id.phoneTV)
        genderTV = findViewById(R.id.gender)
        phoneET = findViewById(R.id.phoneET)
        sexTV = findViewById(R.id.sex)
        sexSp = findViewById(R.id.genderSpinner)
        edit = findViewById(R.id.editButton)
        save = findViewById(R.id.saveButton)
        fUser = FirebaseAuth.getInstance().currentUser!!
        save.isEnabled = false
        getInfo()
        val adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sexSp.adapter = adapter
        sexSp.onItemSelectedListener = this@AccountActivity
        edit.setOnClickListener {
            nameTV.visibility = View.INVISIBLE
            emailTV.visibility = View.INVISIBLE
            phoneTV.visibility = View.INVISIBLE
            sexTV.visibility = View.INVISIBLE
            edit.isEnabled = false
            save.isEnabled = true
            nameET.visibility = View.VISIBLE
            genderTV.visibility = View.VISIBLE
            emailET.visibility = View.VISIBLE
            phoneET.visibility = View.VISIBLE
            sexSp.visibility = View.VISIBLE
            nameET.setText(name)
            emailET.setText(email)
            phoneET.setText(phone)
            image.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
        save.setOnClickListener {
            addUserToDB()
        }
    }

    private fun getInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val user = i.getValue(User::class.java)
                    if (user!!.uid == fUser.uid) {
                        email = user.email.toString()
                        name = user.name.toString()
                        phone = user.phone.toString()
                        sex = user.sex.toString()
                        imageUrl = user.imageUrl.toString()
                        nameTV.text = user.name
                        emailTV.text = user.email
                        phoneTV.text = user.phone
                        sexTV.text = user.sex
                        Picasso.get().load(user.imageUrl).centerInside().resize(120, 120).centerInside().placeholder(R.drawable.account).into(image)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun addImageToStorage() {
        newName = nameET.text.toString().trim()
        newEmail = emailET.text.toString().trim()
        newPhone = phoneET.text.toString().trim()
        sex = sexSp.selectedItem.toString()
        val filename = FirebaseAuth.getInstance().uid
        val storage = FirebaseStorage.getInstance().getReference("image/$filename")
        storage.putFile(selectPicUri!!).addOnCompleteListener {
            Log.i("Sign up", "Successfully uploaded image to database")
            storage.downloadUrl.addOnSuccessListener { uri: Uri -> addImageToDB(uri.toString()) }
        }
    }

    private fun addImageToDB(uri: String?) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("users")
        val hashMap = HashMap<String, String>()
        if (uri != null) {
            hashMap["imageUrl"] = uri
        }
        reference.child(uid).updateChildren(hashMap as Map<String, Any>).addOnSuccessListener {
            Log.i("Account Activity", "Pic updated")
        }
    }

    private fun addUserToDB() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("users")
        val hashMap = HashMap<String, String>()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["phone"] = phone
        hashMap["sex"] = sex
        reference.child(uid).updateChildren(hashMap as Map<String, Any>).addOnSuccessListener {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_LONG).show()
            nameTV.visibility = View.VISIBLE
            emailTV.visibility = View.VISIBLE
            phoneTV.visibility = View.VISIBLE
            sexTV.visibility = View.VISIBLE
            edit.isEnabled = true
            save.isEnabled = false
            nameET.visibility = View.INVISIBLE
            emailET.visibility = View.INVISIBLE
            genderTV.visibility = View.INVISIBLE
            phoneET.visibility = View.INVISIBLE
            sexTV.visibility = View.INVISIBLE
            sexSp.visibility = View.INVISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && requestCode == Activity.RESULT_OK && data != null) {
            selectPicUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectPicUri)
            image.setImageBitmap(bitmap)
            image.setAlpha(0)
            addImageToStorage()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val sex: String = p0!!.getItemAtPosition(p2).toString()
        Log.i("sign up", "$sex is selected")
        this.sex = sex
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}