package com.tiana.neshantiana

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.tiana.neshantiana.common.underlineText
import com.tiana.neshantiana.databinding.FragmentLocationDescriptionDialogBinding

class LocationDescriptionDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentLocationDescriptionDialogBinding
    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        setInformation()
        setListener()
    }


    private fun setInformation() {
        this.binding.fragmentLocationDescriptionDialogCustomerPhoneTv.text =
           underlineText("0233233333")
        this.binding.fragmentLocationDescriptionDialogCustomerPhoneCallTv.text =
            underlineText( "09100364536")

    }

    private fun setListener(){
        this.binding.fragmentLocationDescriptionDialogCustomerPhoneTv.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse(
                    "tel:${
                        this.binding.fragmentLocationDescriptionDialogCustomerPhoneTv.text
                    }"
                )
            this.requireContext().startActivity(intent)
        }

        this.binding.fragmentLocationDescriptionDialogCustomerPhoneCallTv.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse(
                    "tel:${
                        this.binding.fragmentLocationDescriptionDialogCustomerPhoneCallTv.text
                    }"
                )
            this.requireContext().startActivity(intent)
        }
    }


    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        this.binding = DataBindingUtil.inflate(
            LayoutInflater.from(this.requireContext()),
            R.layout.fragment_location_description_dialog,
            null,
            false
        )
        dialogBuilder.setView(binding.root)
        return dialogBuilder.create()
    }

}