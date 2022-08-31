package com.tiana.neshantiana

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.carto.styles.AnimationStyle
import com.carto.styles.AnimationStyleBuilder
import com.carto.styles.AnimationType
import com.carto.styles.MarkerStyleBuilder
import com.carto.utils.BitmapUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.tiana.neshantiana.databinding.FragmentDispersionCustomersLocationMapBinding
import com.tiana.neshantiana.databinding.FragmentLastVisitsLocationMapBinding
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.MapView
import org.neshan.mapsdk.model.Marker
import org.neshan.mapsdk.model.Polyline
import java.text.DateFormat
import java.util.*

class DispersionCustomersLocationMapFragment:Fragment() {

    lateinit var binding: FragmentDispersionCustomersLocationMapBinding
    // map UI element
    var map: MapView? = null

    // marker animation style
    var animSt: AnimationStyle? = null

    // used to track request permissions
    val REQUEST_CODE = 123

    // location updates interval - 1 sec
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000

    // fastest updates interval - 1 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000

    // User's current location
    private var userLocation: Location? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var settingsClient: SettingsClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null
    private var locationCallback: LocationCallback? = null
    private var lastUpdateTime: String? = null

    // boolean flag to toggle the ui
    private var mRequestingLocationUpdates: Boolean? = null
    private var myLocationMarker: Marker? = null
    private var customerLocationMarker: Marker? = null

    private var decodedStepByStepPath: ArrayList<LatLng>? = null
    private var routeOverviewPolylinePoints: ArrayList<LatLng>? = null

    private var onMapPolyline: Polyline?=null

    val previewRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                AppCompatActivity.RESULT_OK -> mRequestingLocationUpdates = true
                AppCompatActivity.RESULT_CANCELED -> {
                    mRequestingLocationUpdates = false
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDispersionCustomersLocationMapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onStart() {
        super.onStart()
        // everything related to ui is initialized here
        initLayoutReferences();
        initLocation();
        startReceivingLocationUpdates();
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        setListener()
    }

    private fun setListener() {
        this.binding.FragmentDispersionCustomersLocationMapMyLocationBtn.setOnClickListener {
            if (userLocation != null) {
                val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                map!!.moveCamera(latLng, 0f)
                map!!.setZoom(15f, 0.25f)
            }
        }
        this.binding.FragmentDispersionCustomersLocationMapBackBtn.setOnClickListener {
            this.requireActivity().onBackPressed()

        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun initLayoutReferences() {
        // Initializing views
        initViews()
        setInformation()
    }

    // We use findViewByID for every element in our layout file here
    private fun initViews() {
        map = binding.FragmentDispersionCustomersLocationMapMapMv

    }

    private fun initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        settingsClient = LocationServices.getSettingsClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (userLocation == null) {
                    // location is received
                    userLocation = locationResult.lastLocation
                    if (userLocation != null) {
                        val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                        map!!.moveCamera(latLng, 0f)
                        map!!.setZoom(15f, 0.25f)
                    }
                }
                userLocation = locationResult.lastLocation
                lastUpdateTime = DateFormat.getTimeInstance().format(Date())
                onLocationChange()
            }
        }
        mRequestingLocationUpdates = false
        locationRequest = LocationRequest()
        locationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)
        locationSettingsRequest = builder.build()
    }

    private fun startLocationUpdates() {
        settingsClient
            ?.checkLocationSettings(locationSettingsRequest!!)
            ?.addOnSuccessListener(requireActivity()) {
                if ((ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED)
                    || mRequestingLocationUpdates == true
                ) {
                    fusedLocationClient!!.requestLocationUpdates(
                        locationRequest!!,
                        locationCallback!!, Looper.myLooper()
                    )
                    onLocationChange()

                }


            }
            ?.addOnFailureListener(requireActivity()) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(requireActivity(), REQUEST_CODE)
                        } catch (sie: IntentSender.SendIntentException) {
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                onLocationChange()
            }
    }

    private fun stopLocationUpdates() {
        // Removing location updates
        fusedLocationClient
            ?.removeLocationUpdates(locationCallback!!)
            ?.addOnCompleteListener(
                requireActivity()
            ) {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireActivity(),
                        "Location updates stopped!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            }
    }

    private fun startReceivingLocationUpdates() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(requireActivity())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    mRequestingLocationUpdates = true
                    startLocationUpdates()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        // open device settings when the permission is
                        // denied permanently
                        openSettings()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        previewRequest.launch(intent)

    }

    private fun onLocationChange() {
        if (userLocation != null) {
            addUserMarker(LatLng(userLocation!!.latitude, userLocation!!.longitude))
        }
    }

    //TODO change with last location
    private fun setInformation() {
        var latLng = LatLng(36.424520, 54.9665126)
        map!!.addMarker(
            createMarker(latLng)
        )
        latLng = LatLng(36.454520, 54.9665126)
        map!!.addMarker(
            createMarker(latLng)
        )
        latLng = LatLng(36.424520, 54.9765126)
        map!!.addMarker(
            createMarker(latLng)
        )
        map!!.setOnMarkerClickListener {
            val locationDescriptionDialogFragment =
                LocationDescriptionDialogFragment()
            locationDescriptionDialogFragment.show(
                requireActivity().supportFragmentManager,
                null
            )
        }
    }

    private fun addUserMarker(loc: LatLng) {
        //remove existing marker from map
        if (myLocationMarker != null) {
            map!!.removeMarker(myLocationMarker)
        }
        // Creating marker style. We should use an object of type MarkerStyleCreator, set all features on it
        // and then call buildStyle method on it. This method returns an object of type MarkerStyle
        val markStCr = MarkerStyleBuilder()
        markStCr.size = 30f
        markStCr.bitmap = BitmapUtils.createBitmapFromAndroidBitmap(
            BitmapFactory.decodeResource(
                resources, org.neshan.mapsdk.R.drawable.ic_marker
            )
        )
        val markSt = markStCr.buildStyle()

        // Creating user marker
        myLocationMarker = Marker(loc, markSt)

        // Adding user marker to map!
        map!!.addMarker(myLocationMarker)
    }

    // This method gets a LatLng as input and adds a marker on that position
    private fun createMarker(loc: LatLng?): Marker? {
        // Creating animation for marker. We should use an object of type AnimationStyleBuilder, set
        // all animation features on it and then call buildStyle() method that returns an object of type
        // AnimationStyle
        val animStBl = AnimationStyleBuilder()
        animStBl.fadeAnimationType = AnimationType.ANIMATION_TYPE_SMOOTHSTEP
        animStBl.sizeAnimationType = AnimationType.ANIMATION_TYPE_SPRING
        animStBl.phaseInDuration = 0.5f
        animStBl.phaseOutDuration = 0.5f
        animSt = animStBl.buildStyle()

        // Creating marker style. We should use an object of type MarkerStyleCreator, set all features on it
        // and then call buildStyle method on it. This method returns an object of type MarkerStyle

        val markStCr = MarkerStyleBuilder()
        markStCr.size = 30f
        markStCr.bitmap = BitmapUtils.createBitmapFromAndroidBitmap(
            BitmapFactory.decodeResource(
                resources, org.neshan.mapsdk.R.drawable.ic_cluster_marker_blue
            )
        )
        // AnimationStyle object - that was created before - is used here
        markStCr.animationStyle = animSt
        val markSt = markStCr.buildStyle()

        // Creating marker
        return Marker(loc, markSt)

    }


}