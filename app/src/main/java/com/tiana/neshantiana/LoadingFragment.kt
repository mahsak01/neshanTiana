package com.tiana.neshantiana

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.tiana.neshantiana.databinding.FragmentLoadingBinding

class LoadingFragment : DialogFragment() {

    private lateinit var binding: FragmentLoadingBinding
    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = 320
        params.height = 320
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }
    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        this.binding = DataBindingUtil.inflate(
            LayoutInflater.from(this.requireContext()),
            R.layout.fragment_loading,
            null,
            false
        )
        this.isCancelable = false
        dialogBuilder.setView(binding.root)
        return dialogBuilder.create()
    }
}