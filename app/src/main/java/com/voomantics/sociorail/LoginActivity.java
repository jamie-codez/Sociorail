package com.voomantics.sociorail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private EditText emailET,passwordET;
    private Button signIn;
    private TextView signUp,recoverPass;
    protected String Email,Password;
    ProgressDialog pd;
    protected FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        pd = new ProgressDialog(this);
        pd.setMessage("Verifying credentials....");
        initViews();
        signIn.setOnClickListener(v -> {
            pd.show();
            Log.i("Login", "login button clicked");
            performSignIn();
        });

        signUp.setOnClickListener(v -> {
            Log.i("Login", "sign up button clicked");
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        recoverPass.setOnClickListener(view -> {
            final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
            final EditText edittext = new EditText(LoginActivity.this);
            edittext.setPadding(25, 0, 25, 20);
            edittext.setMaxWidth(100);
            edittext.setHint("Enter email address...");
            alert.setTitle("Password Recovery");
            alert.setView(edittext);
            alert.setIcon(R.drawable.logo);
            alert.setPositiveButton("Submit", (dialog, whichButton) -> {
                String email = edittext.getText().toString();
                if (!TextUtils.isEmpty(email)) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "Link was sent to your email", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(LoginActivity.this, "Nothing was entered in the field", Toast.LENGTH_SHORT).show();
                }
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            });
            alert.show();
        });
    }
    private void initViews(){
        emailET = findViewById(R.id.email_sign_in);
        passwordET = findViewById(R.id.password_sign_in);
        signIn = findViewById(R.id.sign_in_button);
        signUp = findViewById(R.id.sign_up);
        recoverPass =findViewById(R.id.recover_password);
        mAuth = FirebaseAuth.getInstance();
    }
    protected void performSignIn() {
        Email = emailET.getText().toString().trim();
        Password = passwordET.getText().toString().trim();

        if (Email.isEmpty() || Password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("Login", "signIn method initiated");
            mAuth.signInWithEmailAndPassword(Email, Password)
                    .addOnSuccessListener(authResult -> {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, OfferCategoryActivity.class);
                        startActivity(intent);
                        finish();
                    }).addOnFailureListener(e -> {
                if (e instanceof FirebaseAuthInvalidUserException) {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}