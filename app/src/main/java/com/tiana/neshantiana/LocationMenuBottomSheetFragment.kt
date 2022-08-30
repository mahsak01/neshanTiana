package com.tiana.neshantiana

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tiana.neshantiana.databinding.FragmentLocationMenuBottomSheetBinding

class LocationMenuBottomSheetFragment:BottomSheetDialogFragment() {

    lateinit var binding:FragmentLocationMenuBottomSheetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onResume() {
        super.onResume()
        setListener()
    }

    private fun setListener(){
        this.binding.fragmentLocationMenuBottomSheetAddCustomersLl.setOnClickListener {
            dismiss()
            this.findNavController()
                .navigate(R.id.addLocationCustomerMapFragment)
        }
        this.binding.fragmentLocationMenuBottomSheetMyLocationLl.setOnClickListener {
            dismiss()
            this.findNavController()
                .navigate(R.id.myLocationMapFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_location_menu_bottom_sheet,
            container,
            false
        )
        return binding.root
    }
}