package com.voomantics.sociorail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static com.voomantics.sociorail.Constants.LOCATION_DATA_EXTRA;
import static com.voomantics.sociorail.Constants.LOCATION_REQUEST_CODE;
import static com.voomantics.sociorail.Constants.RECEIVER;
import static com.voomantics.sociorail.Constants.RESULT_DATA_KEY;
import static com.voomantics.sociorail.Constants.SUCCESS_RESULT;

public class DetailActivity extends AppCompatActivity {
    private ImageView offerImage;
    private TextView title, description, dates, prices, LatLong, textAddress, locality, establishment, error;
    private Button enter;
    private Offer offer;
    private ResultReceiver resultReceiver;
    private String Lat, Long;
    double latitude;
    double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        resultReceiver = new AddressResultReceiver(new Handler());

        initViews();
        getData();
        getLocation();
        enter.setOnClickListener(view -> {
            DecimalFormat round = new DecimalFormat("#.###");
            round.setRoundingMode(RoundingMode.CEILING);
            Lat = round.format(latitude);
            Long = round.format(longitude);
            if (offer.getLatitude().equals(Lat) && offer.getLongitude().equals(Long)) {
                Intent intent = new Intent(DetailActivity.this, UsersActivity.class);
                startActivity(intent);
                finish();
            }else {
                error.setVisibility(View.VISIBLE);
                enter.setActivated(false);
            }
        });
    }

    private void initViews() {
        offerImage = findViewById(R.id.offer_image_detail);
        title = findViewById(R.id.title_detail);
        description = findViewById(R.id.description_detail);
        dates = findViewById(R.id.dates_detail);
        prices = findViewById(R.id.location_detail);
        LatLong = findViewById(R.id.LatLong);
        error = findViewById(R.id.error);
        enter = findViewById(R.id.enter_btn);
        textAddress = findViewById(R.id.textAddress);
        locality = findViewById(R.id.locality);
        establishment = findViewById(R.id.establishment);
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent.hasExtra("offer")) {
            offer = intent.getParcelableExtra("offer");
            title.setText(offer.getTitle());
            description.setText(offer.getLiterature());
            String from = offer.getDpFromDate();
            String to = offer.getDpToDate();
            dates.setText(String.format("From: %s\nTo: %s", from, to));
            prices.setText(offer.getLocation());
            locality.setText(String.format("Locality:\n%s", offer.getLocation()));
            establishment.setText(String.format("Est: %s", offer.getEstablishment()));
            Picasso.get().load(offer.getFileSource()).resize(200, 220).placeholder(R.drawable.image).into(offerImage);
        }
        intent.removeExtra("offer");
    }
    
    protected void getLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DetailActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        Toast.makeText(this, "Turn on location services..", Toast.LENGTH_LONG).show();
        LocationServices.getFusedLocationProviderClient(DetailActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(DetailActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            LatLong.setText(String.format("Latitude: %s\nLongitude: %s", latitude, longitude));
                            Location location = new Location("providerNA");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            fetchAddressFromLatLong(location);
                        }
                    }
                }, Looper.getMainLooper());
    }

    private void fetchAddressFromLatLong(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == SUCCESS_RESULT) {
                textAddress.setText(resultData.getString(RESULT_DATA_KEY));
            } else {
                Toast.makeText(DetailActivity.this, resultData.getString(RESULT_DATA_KEY), Toast.LENGTH_SHORT).show();
            }
        }
    }
}