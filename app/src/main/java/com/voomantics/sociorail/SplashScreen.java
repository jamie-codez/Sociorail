package com.voomantics.sociorail;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {
    TextView share;
    SpannableStringBuilder builder = new SpannableStringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        setShareText();
        share = findViewById(R.id.share);
        share.setText(builder, TextView.BufferType.SPANNABLE);

        new Handler().postDelayed(this::checkStatus, 3000);
    }

    private void checkStatus() {
        String user = FirebaseAuth.getInstance().getUid();
        if (user == null) {
            startActivity(new Intent(this, SignUpActivity.class));
        } else {
            startActivity(new Intent(this, OfferCategoryActivity.class));
        }
        finish();
    }

    private void setShareText() {

        String red = "Share\n";
        SpannableString redSpannable = new SpannableString(red);
        redSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, red.length(), 0);
        builder.append(redSpannable);

        String white = "a meal\n";
        SpannableString whiteSpannable = new SpannableString(white);
        whiteSpannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, white.length(), 0);
        builder.append(whiteSpannable);

        String blue = "share a moment!!";
        SpannableString blueSpannable = new SpannableString(blue);
        blueSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, blue.length(), 0);
        builder.append(blueSpannable);

    }
}