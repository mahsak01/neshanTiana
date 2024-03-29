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
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.tiana.neshantiana.data.model.Customer
import com.tiana.neshantiana.data.model.CustomerLocation
import com.tiana.neshantiana.databinding.FragmentRoutingLocationMapBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.neshan.common.model.LatLng
import org.neshan.common.utils.PolylineEncoding
import org.neshan.mapsdk.MapView
import org.neshan.mapsdk.model.Marker
import org.neshan.mapsdk.model.Polyline
import org.neshan.servicessdk.direction.NeshanDirection
import org.neshan.servicessdk.direction.model.NeshanDirectionResult
import retrofit2.*
import java.text.DateFormat
import java.util.*


class RoutingLocationMapFragment : Fragment(), SearchCustomerItemAdapter.EventListener {

    lateinit var binding: FragmentRoutingLocationMapBinding

    private val viewModel: LocationAddressViewModel by viewModel()

    var searchCustomerDialogFragment: SearchCustomerDialogFragment? = null

    var loadingFragment: LoadingFragment? = null

    private var customerMarker: Marker? = null

    private var customer: Customer? = null


    // map UI element
    var map: MapView? = null

    // marker animation style
    var animSt: AnimationStyle? = null

    // used to track request permissions
    private val REQUEST_CODE = 123

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

    private var onMapPolyline: Polyline? = null

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
        binding = FragmentRoutingLocationMapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initLayoutReferences()
        initLocation()
        startReceivingLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        setListener()
        setObserver()
    }

    private fun setObserver() {
        viewModel.customerLocationLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                setInformation(it[0])
            }
        }
    }

    private fun setListener() {
        this.binding.FragmentRoutingLocationMapMyLocationBtn.setOnClickListener {
            if (userLocation != null) {
                val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                map!!.moveCamera(latLng, 0f)
                map!!.setZoom(15f, 0.25f)
            }
        }
        this.binding.FragmentRoutingLocationMapRoutingBtn.setOnClickListener {
            neshanRoutingApi()
        }
        this.binding.FragmentRoutingLocationMapBackBtn.setOnClickListener {
            this.requireActivity().onBackPressed()
        }
        this.binding.FragmentRoutingLocationMapRoutingWithMapBtn.setOnClickListener {

            val uri = java.lang.String.format(
                Locale.ENGLISH, "geo:%f,%f", customerMarker?.latLng?.latitude,
                customerMarker?.latLng?.longitude
            )
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            requireContext().startActivity(intent)
        }
        this.binding.FragmentRoutingLocationMapRoutingWithGoogleMapBtn.setOnClickListener {
            val strUri =
                "http://maps.google.com/maps?q=loc:" + customerMarker?.latLng?.latitude + "," + customerMarker?.latLng?.longitude + " (" + "Label which you want" + ")"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))

            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            )

            startActivity(intent)
        }
        this.binding.FragmentRoutingLocationMapCustomerNameTIET.setOnClickListener {
            searchCustomerDialogFragment =
                SearchCustomerDialogFragment(this)
            searchCustomerDialogFragment!!.show(
                requireActivity().supportFragmentManager,
                null
            )
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun initLayoutReferences() {
        initViews()
    }

    private fun initViews() {
        map = binding.FragmentRoutingLocationMapMapMv
    }

    private fun neshanRoutingApi() {
        NeshanDirection.Builder(
            "service.030ffda5273847449d5d2799184cc70a",
            myLocationMarker?.latLng,
            customerMarker?.latLng
        ).build().call(object : Callback<NeshanDirectionResult> {
            override fun onResponse(
                call: Call<NeshanDirectionResult>,
                response: Response<NeshanDirectionResult>
            ) {
                if (onMapPolyline != null) {
                    map?.removePolyline(onMapPolyline)
                }
                val route = response.body()?.routes?.get(0)
                routeOverviewPolylinePoints =
                    ArrayList(PolylineEncoding.decode(route?.overviewPolyline?.encodedPolyline))
                decodedStepByStepPath = ArrayList()
                // decoding each segment of steps and putting to an array
                for (step in route?.legs?.get(0)!!.directionSteps) {
                    decodedStepByStepPath?.addAll(PolylineEncoding.decode(step.encodedPolyline))
                }
                onMapPolyline = Polyline(routeOverviewPolylinePoints, getLineStyle())
                //draw polyline between route points
                map?.addPolyline(onMapPolyline)
                // focusing camera on first point of drawn line
                binding.FragmentRoutingLocationMapRoutingWithMapBtn.visibility = View.VISIBLE
                binding.FragmentRoutingLocationMapRoutingWithGoogleMapBtn.visibility = View.VISIBLE

            }

            override fun onFailure(call: Call<NeshanDirectionResult>, t: Throwable) {
            }
        })
    }

    private fun getLineStyle(): LineStyle? {
        val lineStCr = LineStyleBuilder()
        lineStCr.color = Color(76.toShort(), 230.toShort(), 76.toShort(), 120.toShort())
        lineStCr.width = 12f
        lineStCr.stretchFactor = 0f
        return lineStCr.buildStyle()
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

    private fun setInformation(customerLocation: CustomerLocation) {
        if (customerMarker != null)
            map!!.removeMarker(customerMarker)
        val latLng = customerLocation.Latitude?.let {
            customerLocation.Longitude?.let { it1 ->
                LatLng(
                    it,
                    it1
                )
            }
        }
        map!!.moveCamera(latLng, 0f)
        map!!.setZoom(15f, 0.25f)
        customerMarker = createMarker(latLng)
        map!!.addMarker(
            customerMarker
        )
        binding.FragmentRoutingLocationMapRoutingBtn.visibility = View.VISIBLE
//        map!!.setOnMarkerClickListener {
//            val locationDescriptionDialogFragment =
//                LocationDescriptionDialogFragment()
//            locationDescriptionDialogFragment.show(
//                requireActivity().supportFragmentManager,
//                null
//            )
//        }
        loadingFragment?.dismiss()
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

    fun setLocationOfCustomer(customer: Customer) {
        customer.customerInfoSN?.let { viewModel.getCustomerLocation(it) }
        this.loadingFragment =
            LoadingFragment()
        loadingFragment?.show(this.childFragmentManager, null)
    }

    override fun click(customer: Customer) {
        searchCustomerDialogFragment?.dismiss()
        if (customer != null) {
            if (customer != this.customer) {
                if (onMapPolyline != null) {
                    map?.removePolyline(onMapPolyline)
                }
                this.customer = customer
                this.binding.FragmentRoutingLocationMapCustomerNameTI.editText!!.text =
                    Editable.Factory.getInstance().newEditable(customer.fullName)
                setLocationOfCustomer(customer)
            }
        } else {
            this.binding.FragmentRoutingLocationMapCustomerNameTI.editText!!.text =
                Editable.Factory.getInstance().newEditable("")
        }
    }
}