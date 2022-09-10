package com.tiana.neshantiana

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tiana.neshantiana.data.model.Customer
import com.tiana.neshantiana.databinding.LayoutCustomerItemBinding

class SearchCustomerItemAdapter(
    private var customers: List<Customer>,
    private val eventListener: EventListener
) :
    RecyclerView.Adapter<SearchCustomerItemAdapter.ViewHolder>() {

    var allCustomers = customers

    inner class ViewHolder(val binding: LayoutCustomerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindingItem(customer: Customer) {
            binding.layoutCustomerItemCustomerIdTv.text = "شماره مشتری : " + customer.customerNo
            binding.layoutCustomerItemCustomerNameTv.text = "نام مشتری : " + customer.fullName
            binding.layoutCustomerItemCustomerAddressTv.text = "آدرس : " + customer.address
            binding.layoutCustomerItemCustomerGroupTv.text =
                "گروه مشتری : " + customer.customerGroupParentDs
            binding.layoutCustomerItemCustomerIdTv.isSelected = true
            binding.layoutCustomerItemCustomerNameTv.isSelected = true
            binding.layoutCustomerItemCustomerAddressTv.isSelected = true
            binding.layoutCustomerItemCustomerGroupTv.isSelected = true

            binding.layoutCustomerItemCustomerCard.setOnClickListener {
                eventListener.click(customer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_customer_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindingItem(customers[position])
    }


    override fun getItemCount(): Int = customers.size

    fun searchCustomer(searchWord: String) {
        customers = allCustomers
        var customerSearch = ArrayList<Customer>()

        for (user in customers) {
            if (user.fullName!!.contains(searchWord))
                customerSearch.add(user)
        }
        customers = customerSearch
        notifyDataSetChanged()
    }

    interface EventListener {
        fun click(customer: Customer)
    }
}