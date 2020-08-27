package com.myfav.rider.ui.home.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myfav.rider.databinding.PacketDetailsBinding
import com.myfav.rider.databinding.TripBinding
import com.myfav.rider.interfaces.NavigationHandler
import com.myfav.rider.interfaces.PacketSelectionHandler
import com.myfav.rider.ui.home.HomeViewModel
import com.myfav.rider.ui.home.data.PacketInfo
import com.myfav.rider.ui.home.fragments.Trip
import com.myfav.rider.utils.UserAlertClient

class PacketDetails : BottomSheetDialogFragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: PacketDetailsBinding
    private lateinit var userAlertClient: UserAlertClient
    private lateinit var details: PacketInfo
    private lateinit var navigationHandler: NavigationHandler

    fun setDetails(packetInfo: PacketInfo ){
        details = packetInfo
    }

    fun setNavigationHandler(handler: NavigationHandler){
        navigationHandler = handler
    }

    companion object {
        private var INSTANCE: PacketDetails? = null

        fun getInstance(): PacketDetails {
            return INSTANCE ?: PacketDetails()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = PacketDetailsBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        userAlertClient = UserAlertClient(activity)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        binding.itemTrip.packetId.text = "" +details.id
        binding.itemTrip.status.text = "" +details.status
        binding.customerName.text = "" +details.customerName
        binding.customerNumber.text = "Contact: " +details.customerNumber
        binding.itemTrip.address.text = "" +details.address

        if(details.status.compareTo("Out For Delivery")==0){
            binding.deliver.visibility = View.VISIBLE
            binding.deliver.text = "Deliver"
            binding.deliver.setOnClickListener(View.OnClickListener {
                dismiss()
                viewModel.setProcessingPacket(details)
                navigationHandler.navigateTo(HomeViewModel.ACTION_SCAN_DELIVERY)
            })
        }else{
            binding.deliver.visibility = View.GONE
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }
}