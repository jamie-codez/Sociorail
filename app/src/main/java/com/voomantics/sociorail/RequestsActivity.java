package com.voomantics.sociorail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private RecyclerView requestRecycler;
    private AdView adView;
    private RequestAdapter adapter;
    private FirebaseUser fUser;
    private List<Request> requestList;
    private ImageView noReqIV;
    private TextView noReqTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        MobileAds.initialize(this);
        adView = findViewById(R.id.adView2);
        requestRecycler = findViewById(R.id.request_recycler);
        noReqIV = findViewById(R.id.no_requestIV);
        noReqTV = findViewById(R.id.no_requestTV);
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        requestRecycler.setHasFixedSize(true);
        requestRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, true));
        adapter = new RequestAdapter(this, getRequests());
        adapter.notifyDataSetChanged();
        requestRecycler.setAdapter(adapter);
        if (requestList.isEmpty()){
            noReqIV.setVisibility(View.VISIBLE);
            noReqTV.setVisibility(View.VISIBLE);
            requestRecycler.setVisibility(View.GONE);
        }
        adapter.setOnItemClickListener(position -> {
            Intent intent = new Intent(this,PairOption.class);
            intent.putExtra("Request",requestList.get(position));
            startActivity(intent);
        });
    }

    private List<Request> getRequests() {
        requestList = new ArrayList<>();
        requestList.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Requests").child(fUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Request request = ds.getValue(Request.class);
                    requestList.add(request);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return requestList;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.request_menu, menu);
        MenuItem item = menu.findItem(R.id.search_users);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.account) {
            startActivity(new Intent(this, AccountActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}