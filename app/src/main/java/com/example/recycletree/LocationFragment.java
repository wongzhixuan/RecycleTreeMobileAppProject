package com.example.recycletree;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationFragment extends Fragment {
    private static final String TAG = "LocationFragment";
    private final Boolean isLocationPermissionGranted = false;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    Marker marker;
    GoogleMap googleMap;
    private DatabaseReference db;


    public LocationFragment() {
        // Required empty public constructor
    }

    public static LocationFragment newInstance() {
        LocationFragment fragment = new LocationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        //Assign variable
        supportMapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.google_map);

        //Initialize fused location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        //Firebase

        db = FirebaseDatabase.getInstance().getReference("mapMarkers");

        //db.push().setValue(marker);

        //getLocationPermission();
        //check permission
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //call method
            getCurrentLocation();
        } else {
            //when permission denied
            //request permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        }

        return view;
    }

    private void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //initialize location
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //When success get
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            Toast.makeText(getContext(), "Map is connected", Toast.LENGTH_SHORT).show();
                            //initialize lat lng
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            //create current location marker
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My location");
                            //zoom map
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            //Add marker on map
                            googleMap.addMarker(markerOptions);
                            if (ContextCompat.checkSelfPermission(getContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            //Current location tools
                            googleMap.setMyLocationEnabled(true);
                            //Create drawable as a marker for recycle bin location
                            Drawable drawable = getResources().getDrawable(R.drawable.recycle_colored);
                            Bitmap bitmap;
                            final Bitmap bmMarker = drawableToBitamp(drawable);
                            Marker[] allMarkers = new Marker[5];

                            //Retrieve Marker Data from firebase
                            db.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.e(TAG, "onDataChange: Loading Markers");
                                    //For multiple markers
                                    int size = (int) snapshot.getChildrenCount();
                                    Marker[] allMarkers = new Marker[size];
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        //Specify java class
                                        MarkerInfo markerInfo = new MarkerInfo();
                                        //array for multiple markers
                                        for (int i = 0; i <= size; i++) {
                                            try {
                                                //Getter and Setter
                                                markerInfo.setLatitude(ds.getValue(MarkerInfo.class).getLatitude());
                                                markerInfo.setLongitude(ds.getValue(MarkerInfo.class).getLongitude());
                                                //retrieve the data from firebase
                                                Double newLatitude = markerInfo.getLatitude();
                                                Double newLongitude = markerInfo.getLongitude();
                                                //convert the data to LatLng
                                                LatLng latLng = new LatLng(newLatitude, newLongitude);

                                                // add updated marker
                                                allMarkers[i] = googleMap.addMarker(new MarkerOptions().position(latLng).title("Recycle Bin").icon(BitmapDescriptorFactory.fromBitmap(bmMarker)));
                                            } catch (Exception ex) {
                                            }
                                            Log.e(TAG, "onDataChange: Markers loaded");
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Firebase error");

                                }
                            });
                        }
                    });
                }
            }
        });
    }

    //For the recycle bin location marker
    private Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission granted
                //call method
                getCurrentLocation();
            }
        }
    }
}
