package com.example.nearme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap,map2;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;
    // Variable for geojson
    List<IndividualLocation> individualLocationArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        {
            checkUserLocationPerission();

        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }
    }

    public boolean checkUserLocationPerission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code );

            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code );

            }
            return  false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case Request_User_Location_Code:
                if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(googleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                 else
                {
                    Toast.makeText(this,"Permission Denied..",Toast.LENGTH_SHORT).show   ();
                }
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if(currentUserLocationMarker!= null)
        {
            currentUserLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("user Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentUserLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));
        getJsonFileFromLocally();
        if(googleApiClient!=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
       if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
           LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
       }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // for geojson.......................


    private String loadGeoJsonFromAsset() {
        try {
            // Load the GeoJSON file from the local asset folder
            String filename = "dealerlocation.geojson";
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (Exception exception) {
            Log.e("MapActivity", "Exception Loading GeoJSON: " + exception.toString());
            exception.printStackTrace();
            return null;
        }
    }

    private void getJsonFileFromLocally() {
        try {

            JSONObject jsonObject = new JSONObject(loadGeoJsonFromAsset());


            JSONArray jsonArray = jsonObject.getJSONArray("employeesList");                  //TODO pass array object name
            Log.e("keshav", "m_jArry -->" + jsonArray.length());


            for (int i = 0; i < jsonArray.length(); i++)
            {


                JSONObject jsonObjectEmployee = jsonArray.getJSONObject(i);



                    String companyId = jsonObjectEmployee.getString("CompanyId");

                String companyName_En = jsonObjectEmployee.getString("CompanyName_En");
                Log.e("keshav", "m_jArry -->" + companyName_En);
                String phone = jsonObjectEmployee.getString("Phone");
                String region = jsonObjectEmployee.getString("Region");
                String province_En = jsonObjectEmployee.getString("Province_En");
                String Lat = jsonObjectEmployee.getString("lat");
                String Lon = jsonObjectEmployee.getString("lon");
                IndividualLocation individualLocation = new IndividualLocation(companyId,companyName_En,phone,region,province_En,Lat,Lon);

                individualLocationArrayList.add(individualLocation);


            }       // for

            Log.d("adtag","ListOfLocation..............====>>"+individualLocationArrayList.size());
            if(individualLocationArrayList.size()>0) {
                for (int i = 0; i < individualLocationArrayList.size(); i++) {

                    Log.d("adtag", "value is===>>>>" + individualLocationArrayList.get(i).lon);

                    LatLng latLng = new LatLng(Double.parseDouble(individualLocationArrayList.get(i).lat),Double.parseDouble(individualLocationArrayList.get(i).lon));
                    MarkerOptions markerOptions1 = new MarkerOptions();
                    markerOptions1.position(latLng);
                    markerOptions1.title("Contact Us:-"+(individualLocationArrayList.get(i).Phone));
                    markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    currentUserLocationMarker = mMap.addMarker(markerOptions1);

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomBy(12));

                    if(googleApiClient!=null)
                    {
                        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
