package com.tiana.neshantiana

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.carto.graphics.Color
import com.carto.styles.*
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
import com.tiana.neshantiana.common.haversine
import com.tiana.neshantiana.databinding.FragmentLocationNearCustomersMapBinding
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.MapView
import org.neshan.mapsdk.model.Circle
import org.neshan.mapsdk.model.Marker
import java.text.DateFormat
import java.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class LocationNearCustomersMapFragment : Fragment() {


    lateinit var binding: FragmentLocationNearCustomersMapBinding

    private val viewModel: LocationAddressViewModel by viewModel()

    // map UI element
    var map: MapView? = null

    // marker animation style
    var animSt: AnimationStyle? = null

    // used to track request permissions
    private val REQUEST_CODE = 123

    var isRefresh = false

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

    private var circle: Circle? = null
    private var loadingFragment: LoadingFragment? = null

    private val previewRequest =
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
        binding = FragmentLocationNearCustomersMapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // everything related to ui is initialized here
        initLayoutReferences()
        initLocation()
        startReceivingLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        this.observeObservers()
        startLocationUpdates()
        setListener()
    }

    private fun setListener() {
        this.binding.FragmentLocationNearCustomersMapMyLocationBtn.setOnClickListener {
            if (userLocation != null) {
                val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                map!!.moveCamera(latLng, 0f)
                map!!.setZoom(15f, 0.25f)
            }
        }
        this.binding.FragmentLocationNearCustomersMapBackBtn.setOnClickListener {
            this.requireActivity().onBackPressed()
        }
        this.binding.FragmentLocationNearCustomersMapRefreshBtn.setOnClickListener {
            if (userLocation != null) {
                isRefresh = true
                refresh()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun initLayoutReferences() {
        // Initializing views
        initViews()
    }

    // We use findViewByID for every element in our layout file here
    private fun initViews() {
        map = binding.FragmentLocationNearCustomersMapMapMv
    }

    private fun observeObservers() {
        this.viewModel.customersAroundMeLiveData.observe(viewLifecycleOwner) {
            it?.forEach { item ->
                val latLng = LatLng(item.Latitude!!.toDouble(), item.Longitude!!.toDouble())
                map!!.addMarker(
                    createMarker(latLng)
                )
            }
            loadingFragment?.dismiss()
        }
    }

    private fun initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        settingsClient = LocationServices.getSettingsClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // location is received
                if (userLocation == null) {
                    userLocation = locationResult.lastLocation
                    val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                    map!!.moveCamera(latLng, 0f)
                    map!!.setZoom(15f, 0.25f)
                    setInformation()
                    lastUpdateTime = DateFormat.getTimeInstance().format(Date())
                    onLocationChange()

                } else if (isRefresh || haversine(
                        userLocation!!.latitude,
                        userLocation!!.longitude,
                        locationResult.lastLocation!!.latitude,
                        locationResult.lastLocation!!.longitude
                    ) >= 5
                ) {
                    userLocation = locationResult.lastLocation
                    lastUpdateTime = DateFormat.getTimeInstance().format(Date())
                    onLocationChange()
                }

                isRefresh = false

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
                when ((e as ApiException).statusCode) {
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

    private fun refresh() {
        this.binding.FragmentLocationNearCustomersMapRefreshBtn.isEnabled = false
        this.binding.FragmentLocationNearCustomersMapRefreshBtn.setColorFilter(
            ContextCompat.getColor(
                this.requireContext(),
                android.R.color.darker_gray
            )
        )
        Handler(Looper.getMainLooper())
            .postDelayed({
                this.binding.FragmentLocationNearCustomersMapRefreshBtn.isEnabled = true
                this.binding.FragmentLocationNearCustomersMapRefreshBtn.setColorFilter(
                    ContextCompat.getColor(this.requireContext(), R.color.white)
                )
            }, 600000)
        setInformation()

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

    // Drawing circle on map
    private fun drawCircle() {

        // Here we use getLineStyle() method to define line styles
        this.circle = Circle(
            myLocationMarker?.latLng,
            620.0,
            Color(130.toShort(), 230.toShort(), 130.toShort(), 120.toShort()),
            getLineStyle()
        )
        // adding the created circle on map
        map?.addCircle(circle)
    }

    private fun getLineStyle(): LineStyle? {
        val lineStCr = LineStyleBuilder()
        lineStCr.color = Color(130.toShort(), 230.toShort(), 130.toShort(), 120.toShort())
        lineStCr.width = 5f
        lineStCr.stretchFactor = 0f
        return lineStCr.buildStyle()
    }

    private fun setInformation() {
        this.loadingFragment =
            LoadingFragment()
        loadingFragment?.show(this.childFragmentManager, null)
        if (circle != null) {
            map?.removeCircle(circle)
            drawCircle()
        }
        viewModel.getCustomersAroundMe(
            userLocation!!.latitude.toString(),
            userLocation!!.longitude.toString(), 500
        )
        map!!.setOnMarkerClickListener {
            for (item in viewModel.customersAroundMeLiveData.value!!) {
                if (item.Latitude == it.latLng.latitude && item.Longitude == it.latLng.longitude) {
                    val locationDescriptionDialogFragment =
                        LocationDescriptionDialogFragment(item)
                    locationDescriptionDialogFragment.show(
                        requireActivity().supportFragmentManager,
                        null
                    )
                    break
                }
            }

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

        if (circle == null)
            drawCircle()


        // Adding user marker to map!
        map!!.addMarker(myLocationMarker)
    }

    // This method gets a LatLng as input and adds a marker on that position
    private fun createMarker(loc: LatLng?): Marker {
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
                resources, R.drawable.location
            )
        )
        // AnimationStyle object - that was created before - is used here
        markStCr.animationStyle = animSt
        val markSt = markStCr.buildStyle()
        // Creating marker
        return Marker(loc, markSt)
    }
}