package edu.washington.bennyn.alarmlearning

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.orhanobut.hawk.Hawk
import org.w3c.dom.Text
import java.util.ArrayList

class LocationAlarmSetter : AppCompatActivity(), OnMapReadyCallback, OnMarkerDragListener, View.OnClickListener, com.google.android.gms.location.LocationListener {
    private var locationManager: LocationManager? = null
    private lateinit var mMap: GoogleMap
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_alarm_setter)
        Hawk.init(this).build()

        val menu = findViewById<com.michaldrabik.tapbarmenulib.TapBarMenu>(R.id.tapBarMenu)

        menu.setOnClickListener {
            menu.toggle()
        }
        setBtnListeners()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create persistent LocationManager reference
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        val locationSetBtn = findViewById<Button>(R.id.locationSetBtn)

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)

        val permission = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ActivityCompat.requestPermissions(this, permission,0)
        buildGoogleApiClient()
        locationSetBtn.setOnClickListener(this)
    }

    @SuppressLint("MissingPermission")
    override fun onLocationChanged(location: Location?) {
        // You can now create a LatLng Object for use with maps
        // val latLng = LatLng(location.latitude, location.longitude)
        Log.d("locationChanged", "" + location!!.latitude + " " + location!!.longitude)
        val categoryView = findViewById<TextView>(R.id.categoryView)
        val alarmName = findViewById<TextView>(R.id.alarmNameView)
        val setLocation = Hawk.get<LatLng>("alarmLocation")
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) // current location
        if (mLocation!!.latitude >= setLocation.latitude - 0.01 &&
                mLocation!!.latitude <= setLocation.latitude + 0.01 &&
                mLocation!!.longitude <= setLocation.longitude + 0.01 &&
                mLocation!!.longitude >= setLocation.longitude - 0.01) {

            // Send intent to receiver when we're near the set location
            val intent = Intent(this, AlarmReceiver::class.java)
            intent.putExtra("message", alarmName.text.toString())
            intent.putExtra("categoryName", categoryView.text.toString())
            sendBroadcast(intent)
        }
    }

    override fun onClick(v: View?) {
        if (!checkGPSEnabled()) {
            Toast.makeText(this, "EnableGPS", Toast.LENGTH_SHORT).show()
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                getLocation();
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else { //show set location when the OK button is pressed
            getLocation()
        }
    }

    // Log.d current location
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val alarmName = findViewById<TextView>(R.id.alarmNameView)
        val categoryView = findViewById<TextView>(R.id.categoryView)
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdates();
        }
        if (Hawk.get<LatLng>("alarmLocation") != null && !alarmName.text.isBlank() && !categoryView.text.isBlank()) {
            val location = Hawk.get<LatLng>("alarmLocation")

            // put alarm and its category into a map to use later
            var map : MutableMap<String, ArrayList<String>> = Hawk.get("mapOfAlarms")
            val givenCategory = categoryView.text.toString()
            var categories = map.keys
            var categoryStats = Hawk.get<MutableMap<String, Array<Int>>>("categoryStats")

            // If the list of categories doesn't contain this category, add it
            if (!categories.contains(givenCategory)) {
                map.set(givenCategory, ArrayList<String>())
                categoryStats.set(givenCategory, arrayOf(0, 0))
                Hawk.put("categoryStats", categoryStats)
            }
            var listOfAlarms = map.get(givenCategory)
            if (!listOfAlarms!!.contains(alarmName.text.toString())) {
                listOfAlarms!!.add(alarmName.text.toString())
            }
            map.set(givenCategory, listOfAlarms)
            Hawk.put("mapOfAlarms", map)
            alarmName.text = ""
            categoryView.text = ""

            Toast.makeText(this, "Set location: " + location!!.latitude + ", " + location!!.longitude, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Fill in both fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient!!.connect()
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                        })
                        .create()
                        .show()
            } else ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val point = LatLng(47.66034376016674, -122.31053721159698)
        Hawk.put("alarmLocation", point)
        mMap.addMarker(MarkerOptions().position(point).title("Move marker to desired location").draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
        mMap.setOnMarkerDragListener(this)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
    }

    override fun onMarkerDragStart(marker : Marker) {

    }

    override fun onMarkerDragEnd(marker : Marker) {
        Log.d("markerLocation", "" + marker.position.latitude + " " + marker.position.longitude)
        Hawk.put("alarmLocation", marker.position)
    }

    override fun onMarkerDrag(marker : Marker) {
        //topText.text = getString(R.string.on_marker_drag, marker.position.latitude, marker.position.longitude)
    }

    // Set menu button listeners
    private fun setBtnListeners() {
        val chartsButton = findViewById<ImageView>(R.id.chartsButton)
        val messageBtn = findViewById<ImageView>(R.id.messageButton)
        val listBtn = findViewById<ImageView>(R.id.listButton)
        val locationBtn = findViewById<ImageView>(R.id.locationButton)

        chartsButton.setOnClickListener {
            val intent = Intent(this, DataViz::class.java)
            startActivity(intent)
        }

        locationBtn.setOnClickListener {
            val intent = Intent(this, LocationAlarmSetter::class.java)
            startActivity(intent)
        }

        messageBtn.setOnClickListener {
            val intent = Intent(this, AlarmList::class.java)
            startActivity(intent)
        }

        listBtn.setOnClickListener {
            val intent = Intent(this, RealList::class.java)
            startActivity(intent)
        }
    }
}