package com.voomantics.sociorail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class UsersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "UsersActivity";
    private RecyclerView usersRecyclerView;
    private TextView joinListTV, noUsers;
    private List<UserRowItem> userList;
    private UsersAdapter adapter;
    private FloatingActionButton addName, removeName;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_activity);
        usersRecyclerView = findViewById(R.id.user_recyclerview);
        removeName = findViewById(R.id.remove_name);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        addName = findViewById(R.id.add_name);
        removeName = findViewById(R.id.remove_name);
        joinListTV = findViewById(R.id.join_list_tv);
        noUsers = findViewById(R.id.no_users);

        addName.setOnClickListener(view -> addName());
        removeName.setOnClickListener(view -> removeName());

        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getUsers();
    }

    public void addName() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query query = reference.orderByChild("uid").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user.getUid().equals(firebaseUser.getUid())) {
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("customer_list");
                        UserRowItem _user = new UserRowItem(user.getName(), user.getUid(), user.getImageUrl(), user.getName().toLowerCase());
                        mRef.child(user.getUid()).setValue(_user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UsersActivity.this, "Successfully added your name to the list", Toast.LENGTH_SHORT).show();
                                    checkStatus();
                                    getUsers();
                                    removeName.setVisibility(View.VISIBLE);
                                    joinListTV.setText("Leave list");
                                    addName.setVisibility(View.GONE);

                                }).addOnFailureListener(e -> Toast.makeText(UsersActivity.this, "Something went wrong try again", Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.wtf(TAG, "" + error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.users_menu, menu);
        MenuItem item = menu.findItem(R.id.search_users);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.requests:
                startActivity(new Intent(this, RequestsActivity.class));
            default:
                System.out.println("Sociarail");
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeName() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("customer_list").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.setValue(null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UsersActivity.this, "Successfully removed your name from the list", Toast.LENGTH_SHORT).show();
                    removeName.setVisibility(View.INVISIBLE);
                    joinListTV.setText("Join list");
                    addName.setVisibility(View.VISIBLE);
                    getUsers();
                });
    }

    public void getUsers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customer_list");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList = new ArrayList<>();
                for (DataSnapshot user : snapshot.getChildren()) {
                    UserRowItem mUser = user.getValue(UserRowItem.class);
                    Log.i(TAG, "fetch" + mUser);
                    if (mUser != null) {
                        userList.add(new UserRowItem(mUser.getUserName(), mUser.getUid(), mUser.getImageUrl(), mUser.getUserName().toLowerCase()));
                    }
                }
                adapter = new UsersAdapter(UsersActivity.this, userList);
                usersRecyclerView.setAdapter(adapter);
                if (userList.isEmpty()) {
                    noUsers.setVisibility(View.VISIBLE);
                    usersRecyclerView.setVisibility(View.GONE);
                }
                adapter.setOnItemClickListener(position -> {
                    UserRowItem user = userList.get(position);
                    String uid = user.getUid();
                    if (!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        Intent intent = new Intent(UsersActivity.this, PairActivity.class);
                        intent.putExtra("user_row_item", userList.get(position));
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.wtf(TAG, error.getMessage());
            }
        });
    }

    private void checkStatus() {
        if (firebaseUser != null) {
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_UID", firebaseUser.getUid());
            editor.apply();
        } else {
            startActivity(new Intent(UsersActivity.this, SignUpActivity.class));
        }
    }

    public void searchUser(String s) {
        Query query = FirebaseDatabase.getInstance().getReference("customer_list").orderByChild("search").startAt(s.toLowerCase()).endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserRowItem user = dataSnapshot.getValue(UserRowItem.class);
                    assert user != null;
                    if (!firebaseUser.getUid().equals(user.getUid())) {
                        UserRowItem mUser = new UserRowItem(user.getUserName(), user.getUid(), user.getImageUrl(), user.getSearch());
                        userList.add(mUser);
                    }
                }

                adapter = new UsersAdapter(UsersActivity.this, userList);
                usersRecyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(position -> {
                    UserRowItem user = userList.get(position);
                    String uid = user.getUid();
                    if (!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        Intent intent = new Intent(UsersActivity.this, PairActivity.class);
                        intent.putExtra("user_row_item", userList.get(position));
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.wtf(TAG, error.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        checkStatus();
        super.onResume();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchUser(query.toLowerCase());
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchUser(newText.toLowerCase());
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeName();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeName();
        startActivity(new Intent(this, DetailActivity.class));
    }
}