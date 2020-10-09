package com.voomantics.sociorail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OffersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private RecyclerView offerRecyclerView;
    private AdView adView;
    private OfferAdapter offerAdapter;
    private ProgressDialog progressDialog;
    private List<Offer> offerList;
    private String clicked;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);
        initViews();
        getData();
        MobileAds.initialize(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        getOffers(clicked);
    }

    protected void getData() {
        Intent intent = getIntent();
        if (intent.hasExtra("clicked")) {
            clicked = intent.getStringExtra("clicked");
        }
        intent.removeExtra("clicked");
    }

    protected void initViews() {
        offerRecyclerView = findViewById(R.id.offer_recyclerView);
        adView = findViewById(R.id.adView);
        offerRecyclerView.setHasFixedSize(true);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        readName();
    }

    protected void readName() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        Query query = ref.orderByChild("uid").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User usr = ds.getValue(User.class);
                    assert usr != null;
                    if (usr.getUid().equals(firebaseUser.getUid())) {
                        String title = String.format("Welcome %s", usr.getName());
                        getSupportActionBar().setTitle(title);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    protected void getOffers(String clicked) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data....");
        progressDialog.show();
        offerList = new ArrayList<>();
        offerList.clear();
                    if (clicked.equals("Food_Offer")) {
                        getFoodOffers();
                    } else {
                        getAdultOffers();
                    }
    }

    private void getAdultOffers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("offers");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Offer offer = ds.getValue(Offer.class);
                    if (offer.getCategory().equals("18+ OFFERS")){
                        offerList.add(offer);
                    }
                    offerAdapter = new OfferAdapter(OffersActivity.this,offerList, null);
                    offerRecyclerView.setHasFixedSize(true);
                    offerRecyclerView.setLayoutManager(new LinearLayoutManager(OffersActivity.this,RecyclerView.VERTICAL,false));
                    offerRecyclerView.setAdapter(offerAdapter);
                    progressDialog.dismiss();
                    offerAdapter.setOnItemClickListener(position -> {
                        Intent intent = new Intent(OffersActivity.this,DetailActivity.class);
                        intent.putExtra("offer",offerList.get(position));
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFoodOffers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("offers");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Offer offer = ds.getValue(Offer.class);
                    if (offer.getCategory().equals("MEALS OFFERS")){
                        offerList.add(offer);
                    }
                    offerAdapter = new OfferAdapter(OffersActivity.this,offerList, null);
                    offerRecyclerView.setHasFixedSize(true);
                    offerRecyclerView.setLayoutManager(new LinearLayoutManager(OffersActivity.this,RecyclerView.VERTICAL,false));
                    offerRecyclerView.setAdapter(offerAdapter);
                    progressDialog.dismiss();
                    offerAdapter.setOnItemClickListener(position -> {
                        Intent intent = new Intent(OffersActivity.this,DetailActivity.class);
                        intent.putExtra("offer",offerList.get(position));
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.offer_menu, menu);
        final MenuItem item = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            searchList(query.toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Food Activity", "" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchList(newText.toLowerCase());
        return false;
    }

    public void searchList(String s) {
        Query query = FirebaseDatabase.getInstance().getReference("customer_list").orderByChild("search").startAt(s.toLowerCase()).endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                offerList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Offer offer = dataSnapshot.getValue(Offer.class);
                    offerList.add(offer);
                }

                offerAdapter = new OfferAdapter( OffersActivity.this,offerList, null);
                offerRecyclerView.setAdapter(offerAdapter);
                offerAdapter.setOnItemClickListener(position -> {
                    Offer offer = offerList.get(position);
                        Intent intent = new Intent(OffersActivity.this, DetailActivity.class);
                        intent.putExtra("user_row_item",offer);
                        startActivity(intent);
                        finish();

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.wtf("OfferActivity", error.getMessage());
            }
        });
    }

}