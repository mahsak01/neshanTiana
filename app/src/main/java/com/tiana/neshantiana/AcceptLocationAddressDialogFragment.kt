package com.tiana.neshantiana

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.tiana.neshantiana.databinding.FragmentAcceptLocationAddressDialogBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class AcceptLocationAddressDialogFragment(private val address:String ,private val eventListener: EventListener) : DialogFragment() {
    private lateinit var binding: FragmentAcceptLocationAddressDialogBinding
    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        setListeners()
        setInformation()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    private fun setInformation() {
            this.binding.fragmentAcceptLocationAddressDialogAddressLocationTv.text= "آدرس :$address"

    }
    private fun setListeners() {
        this.binding.fragmentAcceptLocationAddressDialogCancelBtn.setOnClickListener {
            dismiss()
        }
        this.binding.fragmentAcceptLocationAddressDialogAcceptBtn.setOnClickListener {
            dismiss()
            eventListener.accept(address)
        }
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        this.binding = DataBindingUtil.inflate(
            LayoutInflater.from(this.requireContext()),
            R.layout.fragment_accept_location_address_dialog,
            null,
            false
        )
        dialogBuilder.setView(binding.root)
        return dialogBuilder.create()
    }

    interface EventListener{
        fun accept(address: String)
    }
}