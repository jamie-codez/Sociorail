package com.voomantics.sociorail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

public class OfferCategoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_category);
        FrameLayout food = findViewById(R.id.food);
        FrameLayout alcohol = findViewById(R.id.alcohol);
        food.setOnClickListener(view -> {
            String foodOffer = "Food_Offer";
            Intent intent = new Intent(this, OffersActivity.class);
            intent.putExtra("clicked", foodOffer);
            startActivity(intent);
        });
        alcohol.setOnClickListener(view -> {
            String adultOffer = "Adult_Offer";
            Intent intent = new Intent(this, OffersActivity.class);
            intent.putExtra("clicked", adultOffer);
            startActivity(intent);
        });
    }
}