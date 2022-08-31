package com.tiana.neshantiana

import android.R.attr.label
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.carto.graphics.Color
import com.carto.styles.*
import com.carto.utils.BitmapUtils
import com.tiana.neshantiana.databinding.FragmentSearchLocationCustomerMapBinding
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.MapView
import org.neshan.mapsdk.model.Label
import org.neshan.mapsdk.model.Marker


class SearchLocationCustomerMapFragment : Fragment() {


    lateinit var binding: FragmentSearchLocationCustomerMapBinding

    // map UI element
    var map: MapView? = null

    // marker animation style
    var animSt: AnimationStyle? = null

    var label: Label? = null

    override fun onStart() {
        super.onStart()
        // everything related to ui is initialized here
        initLayoutReferences()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchLocationCustomerMapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    // We use findViewByID for every element in our layout file here
    private fun initViews() {
        map = binding.FragmentSearchLocationCustomerMapMv
    }

    // Initializing layout references (views, map and map events)
    private fun initLayoutReferences() {
        // Initializing views
        initViews()
        setInformation()

        setListener()

    }

    private fun setListener() {
        this.binding.FragmentSearchLocationCustomerBackBtn.setOnClickListener {
            this.requireActivity().onBackPressed()
        }
    }

    //TODO change with last location
    private fun setInformation() {
        var latLng = LatLng(36.424520, 54.9665126)
        map!!.moveCamera(latLng, 0f)
        map!!.setZoom(15f, 0.25f)
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