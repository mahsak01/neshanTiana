package com.tiana.neshantiana

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tiana.neshantiana.R
import com.tiana.neshantiana.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val shareViewModel: LocationShareViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        setListeners()
        setObserver()
    }

    private fun setObserver() {
        shareViewModel.locationLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                this.binding.FragmentMainLocationTv.text = "آدرس : " + it.address
                this.binding.FragmentMainLocationXTv.text = "عرض : " + it.lat
                this.binding.FragmentMainLocationYTv.text = "طول : " + it.lon

            }
        }
    }

    private fun setListeners() {
        binding.FragmentMainMapBtn.setOnClickListener {
            this.findNavController()
                .navigate(R.id.neshanMapFragment)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}
