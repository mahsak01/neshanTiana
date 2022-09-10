package com.tiana.neshantiana

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tiana.neshantiana.data.model.Customer
import com.tiana.neshantiana.databinding.FragmentSearchCustomerDialogBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchCustomerDialogFragment(private val eventListener: SearchCustomerItemAdapter.EventListener) : DialogFragment() {

    private lateinit var binding: FragmentSearchCustomerDialogBinding
    private val viewModel: LocationAddressViewModel by viewModel()
    private var adapter: SearchCustomerItemAdapter? = null
    private var loadingFragment: LoadingFragment? = null

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        setInformation()
        setListener()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setObserver()
        return binding.root
    }

    private fun setObserver() {
        viewModel.customersLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                loadingFragment?.dismiss()
                binding.FragmentSearchCustomerItemsRv.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                adapter = SearchCustomerItemAdapter(it as MutableList<Customer>,eventListener)
                binding.FragmentSearchCustomerItemsRv.adapter = adapter
            }
        }
    }


    private fun setInformation() {
        viewModel.getCustomers()
        this.loadingFragment =
            LoadingFragment()
        loadingFragment?.show(this.childFragmentManager, null)
    }

    private fun setListener() {
        binding.FragmentSearchCustomerSearchTI.editText?.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.searchCustomer(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        this.binding = DataBindingUtil.inflate(
            LayoutInflater.from(this.requireContext()),
            R.layout.fragment_search_customer_dialog,
            null,
            false
        )
        dialogBuilder.setView(binding.root)
        return dialogBuilder.create()
    }


}