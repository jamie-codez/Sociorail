package com.voomantics.sociorail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button picButton,signUpButton;
    private EditText nameET,emailET,phoneET,passwordET;
    private Spinner genderSpinner;
    private TextView signIn;
    private CircleImageView image;
    ProgressDialog pd;
    protected String Name,Email,Phone,Password,Sex;
    private Uri selectedPhotoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(this);
        pd = new ProgressDialog(this);
        pd.setMessage("Registering.....");
        picButton.setOnClickListener(view -> checkPermissionREAD_EXTERNAL_STORAGE(SignUpActivity.this));
        image.setOnClickListener(view -> checkPermissionREAD_EXTERNAL_STORAGE(SignUpActivity.this));
        signIn.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
        signUpButton.setOnClickListener(view -> {
            pd.show();
            performRegister();
            addImageToDatabase();
        });
    }
    private void initViews(){
        picButton = findViewById(R.id.pic_button);
        signUpButton = findViewById(R.id.sign_up_button);
        nameET = findViewById(R.id.name_sign_up);
        phoneET = findViewById(R.id.phone_sign_up);
        emailET = findViewById(R.id.email_sign_up);
        passwordET = findViewById(R.id.password_sign_up);
        genderSpinner = findViewById(R.id.spinner);
        image = findViewById(R.id.circle_image);
        signIn = findViewById(R.id.sign_in);
    }

    public void checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        }
    }

    public void showDialog(final String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                (dialog, whichBtn) -> ActivityCompat.requestPermissions((Activity) context,
                        new String[]{permission},
                        Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE));
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            } else {
                Toast.makeText(SignUpActivity.this, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void performRegister() {
        Email = emailET.getText().toString().trim();
        Phone = phoneET.getText().toString().trim();
        Password = passwordET.getText().toString().trim();

        if (Email.isEmpty() || Password.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            emailET.setError("Invalid email address");
            emailET.setFocusable(true);
            pd.dismiss();
        } else {
            Register(Email, Password);
        }
    }

    private void Register(String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addImageToDatabase();
                        Toast.makeText(SignUpActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            if (e instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(SignUpActivity.this, "User already exists,consider logging in", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedPhotoUri);
                image.setImageBitmap(bitmap);
                image.setAlpha(0);
                picButton.setVisibility(View.INVISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addImageToDatabase() {
        Name = nameET.getText().toString().trim();
        Sex = genderSpinner.getSelectedItem().toString();
        if (selectedPhotoUri == null) return;
        String filename = FirebaseAuth.getInstance().getUid();
        final StorageReference storage = FirebaseStorage.getInstance().getReference("image/" + filename);
        storage.putFile(selectedPhotoUri).addOnCompleteListener(task -> {
            Log.i("Sign up", "Successfully uploaded image to database");
            storage.getDownloadUrl().addOnSuccessListener(uri -> AddUserToDB(uri.toString()));
        });
    }

    public void AddUserToDB(String uri) {
        pd.setMessage("Finalizing registration...");
        pd.show();
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) return;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users");
        User user = new User(uid,Name,Email,Phone,Sex,uri);
        reference.child(uid).setValue(user).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(SignUpActivity.this, OfferCategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            pd.dismiss();
            finish();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String sex = adapterView.getItemAtPosition(i).toString();
        Log.i("sign up", sex + " is selected");
        Sex = sex;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}