package com.voomantics.sociorail;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Sociorail extends Application {
    String partnerUid;
    FirebaseUser fUser;

    @Override
    public void onCreate() {
        super.onCreate();
        listenForRequests();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    public void listenForResponse(String partnerUid,String partnerName,String myUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Requests").child(partnerUid).child(myUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Request request = ds.getValue(Request.class);
                    if (request.getResponse().equals("Accepted")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendOreoNotification(" accepted",partnerName);
                            Intent intent = new Intent(getApplicationContext(),RedeemTokenActivity.class);
                            intent.putExtra("Request",request);
                            startActivity(intent);
                        } else {
                            sendNotification(" accepted",partnerName);
                            Intent intent = new Intent(getApplicationContext(),RedeemTokenActivity.class);
                            intent.putExtra("Request",request);
                            startActivity(intent);
                        }
                    } else if (request.getResponse().equals("Rejected")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendOreoNotification(" rejected",partnerName);
                        } else {
                            sendNotification(" rejected",partnerName);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void listenForRequests() {
        final String[] PartnerName = new String[1];
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests").child(fUser.getUid());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Request request = ds.getValue(Request.class);
                    if (ds.getKey()!=previousChildName){
                        PartnerName[0] = request.getToName();
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                            new Sociorail().mSendOreoNotification(" sent you a request...", PartnerName[0]);
                        }else {
                            new Sociorail().mSendNotification(" sent you a request...", PartnerName[0]);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendOreoNotification(String msg,String partnerName) {
        String icon = String.valueOf(R.drawable.logo);
        int j = Integer.parseInt(partnerName.replaceAll("[\\D]", ""));
        Intent intent = new Intent(getApplicationContext(), RequestsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", partnerUid);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification("Sociorail", partnerName + msg + " request...", pendingIntent,
                defaultSound, icon);
        int i = 0;
        if (j > 0) {
            i = j;
        }
        oreoNotification.getManager().notify(i, builder.build());
    }

    private void sendNotification(String msg,String partnerName) {
        String icon = String.valueOf(R.drawable.logo);
        int j = Integer.parseInt(partnerName.replaceAll("[\\D]", ""));
        Intent intent = new Intent(getApplicationContext(), RequestsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", partnerUid);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle("Sociorail")
                .setContentText(partnerName + msg + " request...")
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0;
        if (j > 0) {
            i = j;
        }
        noti.notify(i, builder.build());
    }

    public void mSendOreoNotification(String msg,String partnerName) {
        String icon = String.valueOf(R.drawable.logo);
        int j = Integer.parseInt(partnerName.replaceAll("[\\D]", ""));
        Intent intent = new Intent(getApplicationContext(), PairActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", partnerUid);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification("Sociorail", partnerName + msg , pendingIntent,
                defaultSound, icon);
        int i = 0;
        if (j > 0) {
            i = j;
        }
        oreoNotification.getManager().notify(i, builder.build());
    }

    public void mSendNotification(String msg,String partnerName) {
        String icon = String.valueOf(R.drawable.logo);
        int j = Integer.parseInt(partnerName.replaceAll("[\\D]", ""));
        Intent intent = new Intent(getApplicationContext(), PairActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", partnerUid);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle("Sociorail")
                .setContentText(partnerName + msg)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0;
        if (j > 0) {
            i = j;
        }
        noti.notify(i, builder.build());
    }
}
