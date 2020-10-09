package com.voomantics.sociorail;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class PairActivity extends AppCompatActivity {
    private ImageView jigsaw;
    private ProgressBar buffer;
    private TextView requestSentTV, name, mTimer;
    private Button sendRequest;
    private UserRowItem user;
    private String partnerUid, myUid, myName, partnerName, myImageUrl;
    private FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);
        initViews();
        getData();
        sendRequest.setOnClickListener(view -> request());
    }

    private void initViews() {
        jigsaw = findViewById(R.id.jigsaw);
        buffer = findViewById(R.id.buffer);
        requestSentTV = findViewById(R.id.requestTV);
        sendRequest = findViewById(R.id.request_button);
        mTimer = findViewById(R.id.timer);
        name = findViewById(R.id.name);
        fUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent.hasExtra("user_row_item")) {
            user = intent.getParcelableExtra("user_row_item");
            name.setText(user.getUserName());
            partnerUid = user.getUid();
            myUid = fUser.getUid();
        }
        intent.removeExtra("user_row_item");
    }

    private void request() {
        requestSentTV.setVisibility(View.VISIBLE);
        buffer.setVisibility(View.VISIBLE);
        sendRequest.setVisibility(View.INVISIBLE);
        jigsaw.setVisibility(View.GONE);
        partnerName = user.getUserName();
        partnerUid = user.getUid();
        readName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests");
        Request request = new Request(myUid, myName, myImageUrl, partnerUid, partnerName, "request");
        reference.child(partnerUid).child(myUid).child(myUid).setValue(request).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Request sent please wait...", Toast.LENGTH_SHORT).show();
            CountDownTimer timer = new CountDownTimer(300000, 1000) {
                @Override
                public void onTick(long l) {
                    long seconds = l / 1000;
                    long mins = seconds / 60;
                    long secs = seconds % 60;
                    mTimer.setText(String.valueOf(mins) + " : " + String.valueOf(secs));
                    if (mins <= 1) {
                        mTimer.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onFinish() {
                    timedOut();
                    mTimer.setText("5 : 00");
                    mTimer.setTextColor(getResources().getColor(R.color.yellow));
                }
            };
            timer.start();
            new Sociorail().listenForResponse(partnerUid, partnerName, myUid);
        });
    }

    protected void readName() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        Query query = ref.orderByChild("uid").equalTo(myUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User usr = ds.getValue(User.class);
                    assert usr != null;
                    if (usr.getUid().equals(myUid)) {
                        myName = usr.getName();
                        myImageUrl = usr.getImageUrl();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void timedOut() {
        String res = "timed out";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests").child(partnerUid).child(myUid);
        Map<String, Object> map = new HashMap();
        map.put("response", res);
        reference.child(myUid).updateChildren(map).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Request Timed out..", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timedOut();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timedOut();
        startActivity(new Intent(this,UsersActivity.class));
    }
}