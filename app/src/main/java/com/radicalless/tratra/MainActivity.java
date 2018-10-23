package com.radicalless.tratra;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.radicalless.tratra.fragments.HomeFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener {

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    // view references
    LinearLayout bottomMenuBar;

    // Menu Bar
    ImageButton mFeedButton;
    ImageButton mWorldButton;
    LinearLayout mCheckInButton;
    ImageButton mMapButton;
    ImageButton mHomeButton;
    ProgressBar mCheckInProgress;

    // Fragment controllers
    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;

    // Fragment classes
    HomeFragment homeFragment;

    PlaceDetectionClient placeDetectionClient;
    LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTINGS = 1;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        placeDetectionClient = Places.getPlaceDetectionClient(getApplicationContext());

        Log.d("TOKEN: ", "MainActivity started");
        sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sp.edit();

        // check for session availability
        if (sp.getString("sessionToken", null) == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            // check if activated
            if (!sp.getBoolean("activated", false)) {
                startActivity(new Intent(MainActivity.this, NotActivated.class));
                finish();
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        // initialize references
        bottomMenuBar = findViewById(R.id.BottomMenuBar);

        // menu bar button references
        mFeedButton = findViewById(R.id.MBarFeedButton);
        mWorldButton = findViewById(R.id.MBarWorldButton);
        mCheckInButton = findViewById(R.id.MBarRecordingButton);
        mMapButton = findViewById(R.id.MBarMapButton);
        mHomeButton = findViewById(R.id.MBarHomeButton);
        mCheckInProgress = findViewById(R.id.checkInProgress);

        // setup fragment controllers
        fragmentManager = getSupportFragmentManager();

        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCheckInButtonClick();
            }
        });

        // menu bar button listeners
        for (int i = 0; i < bottomMenuBar.getChildCount(); i++) {
            Log.d("Main", bottomMenuBar.getChildAt(i).getClass().getName());
            if (bottomMenuBar.getChildAt(i).getClass().getName().equals("android.support.v7.widget.AppCompatImageButton")) {
                Log.d("Main", "image button class");

                Log.d("Main", "IN");
                final ImageButton imageButton = (ImageButton) bottomMenuBar.getChildAt(i);
                final Context context = this;
                bottomMenuBar.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // make all color of menuBar buttons to be grey
                        // make the selected button color to be active color if it's not new post button
                        turnOffAllButton();
                        imageButton.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
                        // call for custom click function
                        if (getResources().getResourceEntryName(imageButton.getId()).equals("MBarFeedButton")) {
                            handleFeedButtonClick();
                        }

                        if (getResources().getResourceEntryName(imageButton.getId()).equals("MBarWorldButton")) {
                            Log.d("Main", "WORLD");
                            handleWorldButtonClick();
                        }

                        if (getResources().getResourceEntryName(imageButton.getId()).equals("MBarMapButton")) {
                            handleMapButtonClick();
                        }

                        if (getResources().getResourceEntryName(imageButton.getId()).equals("MBarHomeButton")) {
                            handleHomeButtonClick();
                            Log.d("Main", "HOME CLICKED");
                        }
                    }
                });
            }
        }

        // set default activity
        mHomeButton.performClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_logout) {
            editor.remove("sessionToken");
            editor.commit();
            Log.d("UAAAAAAAAAAAAAAAAAA", "CHanges: " + sp.getString("sessionToken", "NULL"));
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    String placeName = "", placeType = "", time = "";

    public void turnOffAllButton() {
        for (int i = 0; i < bottomMenuBar.getChildCount(); i++) {
            if (bottomMenuBar.getChildAt(i).getClass().getName().equals("android.support.v7.widget.AppCompatImageButton")) {
                ImageButton imageButton = (ImageButton) bottomMenuBar.getChildAt(i);
                imageButton.setColorFilter(ContextCompat.getColor(this, R.color.colorOff));
            }
        }
    }

    void handleFeedButtonClick() {

    }

    void handleWorldButtonClick() {

    }

    void handleMapButtonClick() {

    }

    void switchVisibility(View v1, View v2, boolean status) {
        v1.setVisibility((status ? View.VISIBLE : View.GONE));
        v2.setVisibility((status ? View.GONE : View.VISIBLE));
    }

    void handleHomeButtonClick() {
        fragmentTransaction = fragmentManager.beginTransaction();
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        fragmentTransaction.replace(R.id.FragmentContainer, homeFragment);
        fragmentTransaction.commit();
    }

    void handleCheckInButtonClick() {
        switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), true);
        // check for location permission
        if(isLocationPermissionGranted()) {
            getUserLocation();
        } else {
            requestLocationPermission();
        }
    }

    public boolean isLocationPermissionGranted() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    public void requestLocationPermission() {
        Log.d("PostActivity", "requesting location permission...");
        // request for permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   getUserLocation();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Unable to check-in")
                            .setMessage("We need your permission to be able to check you in.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
        }
    }

    public void getUserLocation() {
        Log.d("PostActivity", "Internet exists");

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Main", "Location changed.");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Log.d("Main", "ERR: NO permission");

                    new AlertDialog.Builder(context)
                            .setTitle("Unable to check-in")
                            .setMessage("We need your permission to be able to check you in.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();

                    return;
                }

                if(isInternetConnected()) {
                    checkIn(location.getLatitude(), location.getLongitude());
                } else {
                    checkInOffline(location.getLatitude(), location.getLongitude());
                }

                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("Main", "Status changed");
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("Main", "PRovider enabled");
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("Main", "Provider Disabled " + s);
            }
        };
        Log.d("PostActivity", "Request for location.");
        Location response = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (response != null) {
            if(isInternetConnected()) {
                checkIn(response.getLatitude(), response.getLongitude());
            } else {
                checkInOffline(response.getLatitude(), response.getLongitude());
            }

        } else {
            // get current userLat long
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            Log.d("PostActivity", "Location update requested.");

        }
    }

    public void checkIn(final double lat, final double lon) {
        Log.d("PostActivity", "Checking in...");

        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PostActivity", "Location permission not granted!");
            switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // get place info
        final Task<PlaceLikelihoodBufferResponse> currentPlaces = placeDetectionClient.getCurrentPlace(null);
        currentPlaces.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                Log.d("PostActivity", "Obtaining current places completed.");

                try {
                    PlaceLikelihoodBufferResponse response = task.getResult(ApiException.class);
                    if(response.getCount() > 0) {
                        placeName = response.get(0).getPlace().getAddress().toString();
                        placeType = response.get(0).getPlace().getPlaceTypes().get(0).toString();
                    } else {
                        placeName = "Unknown";
                        placeType = "Unknown";
                    }

                    Log.d("PostActivity", "Succeed obtaining location info.");

                    // get current time
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    time = simpleDateFormat.format(c.getTime());

                    // send it to database
                    final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url = "http://indomotorart.com/tratra/RESTApi/checkIn.php";
                    final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("PostActivity", "Volley response success. PlaceType: " + placeType);
                            Log.d("PostActivity", time);

                            // parse response JSON to array
                            JSONObject responseJSON = null;
                            Log.d("Main", "RESPONSE: " + response);

                            try {
                                responseJSON = new JSONObject(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);
                            }

                            boolean status = false;

                            if(responseJSON != null) {
                                try {
                                    status = Boolean.valueOf(responseJSON.getString("status"));

                                    if(status) {
                                        switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);

                                        Log.d("PostActivity", "Success sending data to server");

                                        Toast.makeText(getApplicationContext(), "Check-In succeed", Toast.LENGTH_LONG).show();
                                        Log.d("Main", responseJSON.getString("updateDistance"));
                                        homeFragment.updateData();

                                    } else {
                                        if(responseJSON.getString("errorCode").equals("invalid_code_ethic_check_in_at_same_previous_place")) {
                                            new AlertDialog.Builder(context)
                                                    .setTitle("Unable to check-in.")
                                                    .setMessage("You have already checked-in at the same place before.")
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .show();

                                        }

                                        Log.d("PostActivity", "Failed to send data to database SUCCESS ON SERVER!. " + responseJSON.getString("msg"));
                                        Toast.makeText(getApplicationContext(), "Check-In failed", Toast.LENGTH_LONG).show();

                                    }
                                } catch (JSONException e) {
                                    switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);
                                    Log.d("PostActivity", "EXCEPTION: Failed to send data to server");
                                    Toast.makeText(getApplicationContext(), "Check-In failed", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();

                                }
                            } else {
                                switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);
                                Log.d("PostActivity", "Unable to process response!");
                                new AlertDialog.Builder(context)
                                        .setTitle("Unable to check-in")
                                        .setMessage("An error occurred while trying to process your check-in. ERR_NULL ")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .show();

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);
                            Log.d("PostActivity", "Error response");
                            new AlertDialog.Builder(getApplicationContext())
                                    .setTitle("Unable to check-in")
                                    .setMessage("An error occurred while trying to process your check-in. Error code: ERR_RESPONSE")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .show();

                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String,String>();
                            params.put("token", sp.getString("sessionToken", "0"));
                            params.put("placename", placeName);
                            params.put("placetype", placeType);
                            params.put("lat", ""+lat);
                            params.put("lon", ""+lon);
                            params.put("datetime", time);
                            return params;

                        }
                    };
                    queue.add(stringRequest);

                } catch (ApiException e) {
                    switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);
                    e.printStackTrace();

                    if(e.getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
                        Log.d("PostActivity", "Failed to getCurrentPlace(): network error.");

                        new AlertDialog.Builder(getApplicationContext())
                                .setTitle("Unable to check-in")
                                .setMessage("Network error occurred. Make sure you have an internet access.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();

                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle("Unable to check-in")
                                .setMessage("An error occurred while trying to process your check-in. ERR_SEND")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();

                    }
                }
            }
        });

    }

    public void checkInOffline(double lat, double lon) {
        // store it to cache
        if(sp.getStringSet("checkInCache", null) == null) {
            Set<String> checkInCache = new HashSet<String>();

            String newLine = lat+","+lon;

            checkInCache.add(newLine);
            editor.putStringSet("checkInCache", checkInCache);

        } else {
            // get from cache first
            Set<String> checkInCache = sp.getStringSet("checkInCache", null);
            String newLine = lat+","+lon;
            checkInCache.add(newLine);
            editor.putStringSet("checkInCache", checkInCache);

        }

        new AlertDialog.Builder(context)
                .setTitle("Your check-in saved offline")
                .setMessage("You don't have internet access, your check-in saved offline. Your data will be updated after you have an internet access.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public void checkAndAskForGPS() {
        Log.d("PostActivity", "checking for GPS...");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    Log.d("PostActivity", "succeed checking gps.");
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    if(response.getLocationSettingsStates().isGpsUsable()) {
                        handleInternetExistence();
                    }
                } catch (ApiException e) {
                    switchVisibility(mCheckInButton.getChildAt(0), mCheckInButton.getChildAt(1), false);
                    Log.d("PostActivity", "Status code: " + e.getStatusCode());
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            handleInternetExistence();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // show up dialog
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(
                                        MainActivity.this,
                                        REQUEST_CHECK_SETTINGS
                                );
                            } catch (IntentSender.SendIntentException ex) {

                            } catch (ClassCastException exc) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    public boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

}
