package edu.msu.gechang1.cse476map;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.LocationListener;

import java.io.InputStream;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener

//public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private boolean check_end=false;
    private String CurrentPlayer;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location mLastLocation;
    private int ownstar=0;
    private ArrayList<LatLng> mStarLocations = new ArrayList<>();
    private ArrayList<Cloud.Item> arrayItem = new ArrayList<>();
    private Cloud.User u = new Cloud.User();

    private boolean check = true;
    private int collected_num_stars=0;

    private int idStar = -1;

    TextView text_playername, text_stars;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        CurrentPlayer = getIntent().getStringExtra("username");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        text_playername = (TextView)findViewById(R.id.textPlayerName);
        text_stars = (TextView)findViewById(R.id.textStars);
        String player_message="User: "+CurrentPlayer;
        text_playername.setText(player_message);
        update_num_stars();
        
        // 1
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){

                    Cloud cloud = new Cloud();
                    arrayItem = cloud.LoadStars();
                    u = cloud.loadFromCloud(CurrentPlayer);

                    mStarLocations = new ArrayList<>();
//                Cloud cloud = new Cloud();
//                arrayItem = cloud.LoadStars();
                    for(int i = 0; i < arrayItem.size(); i++){
                        if(arrayItem.get(i).status.equals("0")){
                            mStarLocations.add(new LatLng(Double.valueOf(arrayItem.get(i).lat),Double.valueOf(arrayItem.get(i).lng)));
                        }
                    }
                    if (u.numStars.equals("3"))
                    {
                        cloud.endGame(u.name);
                    }

//                    if(u.status.equals("1")){
//                        //win
//                        Intent intent = new Intent(MapsActivity.this,EndGameActivity.class);
//                        intent.putExtra("status",u.status);
//                        startActivity(intent);

//                    }
//                    else if(u.status.equals("2")){
//                        //lose
//                        Intent intent = new Intent(MapsActivity.this,EndGameActivity.class);
//                        intent.putExtra("status",u.status);
//                        startActivity(intent);

//                    }

                    final int id = CheckDistance();

                    idStar = id;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                     if(u.status.equals("1")){
                        //win
                        Intent intent = new Intent(MapsActivity.this,EndGameActivity.class);
                        intent.putExtra("status",u.status);
                        startActivity(intent);
                        check_end=true;
                        return;

                    }
                    else if(u.status.equals("2")){
                        //lose
                        Intent intent = new Intent(MapsActivity.this,EndGameActivity.class);
                        intent.putExtra("status",u.status);
                        startActivity(intent);
                        check_end=true;
                        return;


                    }
                            update_num_stars();
                            boolean checkstatus = true;
                            for(int i = 0; i < arrayItem.size(); i++){
                                if(id == Integer.valueOf(arrayItem.get(i).id)){
                                    if(arrayItem.get(i).status.equals("1")){
                                        checkstatus = false;
                                    }
                                    else{
                                        checkstatus = true;
                                    }
                                }
                            }

                            if(id != -1 && checkstatus){
                                getCollectButton().setEnabled(true);
                            }
                            else{
                                getCollectButton().setEnabled(false);
                            }
//                            if(id != -1){
//                                getCollectButton().setEnabled(true);
//                            }
//                            else{
//                                getCollectButton().setEnabled(false);
//                            }


                            int current_num_stars=0;
                            for(int i = 0; i < arrayItem.size(); i++){
                                if(arrayItem.get(i).status.equals("1")){
                                    current_num_stars+=1;
                                }
                            }
                            if(current_num_stars != collected_num_stars){
                                check = true;
                                collected_num_stars = current_num_stars;
                            }



                            if(check){
                                mMap.clear();
                                for (int i = 0; i < mStarLocations.size(); i++) {
                                    LatLng starlocation = mStarLocations.get(i);
                                    placeMarkerOnMap(starlocation);
                                }
                                check = false;
                            }
                        }
                    });
                    if(check_end==true)
                    {
                        break;
                    }
                }
            }
        }).start();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LatLng myPlace = new LatLng(40.73, -73.99);  // this is New York
        mMap.addMarker(new MarkerOptions().position(myPlace).title("My Favorite City"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPlace));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 12));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        setUpMap();
    }

//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setUpMap();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 2
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 3
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // 1
        mMap.setMyLocationEnabled(true);

        // 2
        LocationAvailability locationAvailability =
                LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            // 3
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // 4
            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation
                        .getLongitude());


                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));

            }
        }
    }

    protected void placeMarkerOnMap(LatLng location) {
        // 1
        MarkerOptions markerOptions = new MarkerOptions().position(location);

        Bitmap star = BitmapFactory.decodeResource(getResources(), R.mipmap.finalstar);
        Bitmap resized_star = Bitmap.createScaledBitmap(star, 120, 120, true);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resized_star));
        // 2
        mMap.addMarker(markerOptions);
    }

    private void update_num_stars()
    {
        String star_message="You find "+u.numStars+" stars! Keep Going!";
        text_stars.setText(star_message);
    }


//    private void GetStars()
//    {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mMap.clear();
//                        for (int i = 0; i < mStarLocations.size(); i++) {
//
//                            LatLng starlocation = mStarLocations.get(i);
//                            placeMarkerOnMap(starlocation);
//                        }
//                    }
//                });
//            }
//        }).start();
//    }

    private int CheckDistance() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return -1;
        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        for (int i = 0; i < arrayItem.size(); i++) {
            Location loc1 = new Location("");
            loc1.setLatitude(Double.valueOf(arrayItem.get(i).lat));
            loc1.setLongitude(Double.valueOf(arrayItem.get(i).lng));
            if (l == null){
                return -1;
            }
            float distance = l.distanceTo(loc1);

            if(distance <= 50.f){
                return Integer.valueOf(arrayItem.get(i).id);
            }
        }
        return -1;
    }

    public void onCollect(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();
                if(idStar != -1){
                    cloud.changeStarStatus(idStar, CurrentPlayer);
                }

            }
        }).start();
    }



    public Button getCollectButton(){
        return (Button)findViewById(R.id.start_tracking);
    }

    public void onSurrender(View view)
    {
        //surrender
        Intent intent = new Intent(MapsActivity.this,EndGameActivity.class);
        intent.putExtra("status","3");
        startActivity(intent);
    }

}

