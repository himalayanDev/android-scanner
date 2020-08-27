package com.myfav.rider.ui.home.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.myfav.rider.databinding.TripBinding
import com.myfav.rider.interfaces.NavigationHandler
import com.myfav.rider.interfaces.PacketDetailsRequestHandler
import com.myfav.rider.interfaces.PacketSelectionHandler
import com.myfav.rider.interfaces.UserActionHandler
import com.myfav.rider.ui.home.HomeViewModel
import com.myfav.rider.ui.home.HomeViewModel.Companion.ACTION_HOME
import com.myfav.rider.ui.home.adapters.TripAdapter
import com.myfav.rider.ui.home.adapters.UserSelectionActionAdapter
import com.myfav.rider.ui.home.data.PacketInfo
import com.myfav.rider.utils.SharedPrefsClient
import com.myfav.rider.utils.UserAlertClient
import org.json.JSONObject


class Trip : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: TripBinding
    private lateinit var userAlertClient: UserAlertClient
    private lateinit var gridAdapter: TripAdapter
    private lateinit var barCodes : ArrayList<String>
    private lateinit var mPacketSelectionHandler:PacketSelectionHandler
    private lateinit var sharedPrefsClient: SharedPrefsClient
    private lateinit var navigationHandler: NavigationHandler

    companion object {
        private var INSTANCE: Trip? = null

        fun getInstance(): Trip {
            return INSTANCE ?: Trip()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationHandler = context as NavigationHandler
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TripBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    fun setBarcodeList(list: ArrayList<String> ){
        barCodes = list
    }

    fun setPacketSelectionHandler(handler:PacketSelectionHandler){
        mPacketSelectionHandler = handler
    }

    private fun init() {
        sharedPrefsClient = SharedPrefsClient(context)
        userAlertClient = UserAlertClient(activity)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        if(sharedPrefsClient.getTripStatus()){
            resetAdapter()
        }else{
            binding.summary.visibility = View.INVISIBLE
            userAlertClient.showWaitDialog("Creating trip...")
            viewModel.loadPacketList(barCodes,packetDetailsRequestHandler)
        }
    }

    private var userSelectionListener = UserSelectionActionAdapter.UserSelectionListener {
        Log.i("_UserSelectionListener", "pos: $it")
        viewModel.setFragment(viewModel.getSelectionList()[it].Action)

//        var adminClient = AdministrationClient()
//        adminClient.createUser(context)
    }

    private var packetDetailsRequestHandler:PacketDetailsRequestHandler = object :PacketDetailsRequestHandler{
        override fun onResult(result: JSONObject?) {
            sharedPrefsClient.setTripStatus(true)
            sharedPrefsClient.setTripDetails(result.toString())
            userAlertClient.closeWaitDialog()
            resetAdapter()
        }

        override fun onError(message: String?) {
            userAlertClient.closeWaitDialog()
            userAlertClient.showDialogMessage("Error Alert!",message,false)
        }
    }

    private fun resetAdapter(){
        var tripDetails = sharedPrefsClient.getTripDetails()
        binding.summary.visibility = View.VISIBLE
        binding.deliveryCount.text = "Delivered: 0/" + (tripDetails.packetList?.size ?: 0)
        binding.selectionGrid.layoutManager = GridLayoutManager(context, 1)
        gridAdapter = TripAdapter(
                context,
                tripDetails.packetList,
                mPacketSelectionHandler
        )
        binding.selectionGrid.adapter = gridAdapter

        var notDeliveredCount = 0
        for(packet in tripDetails.packetList){
            if(packet.status.compareTo("Delivered")!=0)
                notDeliveredCount++
        }
        if(notDeliveredCount == 0) {
            var handler : UserActionHandler = object  : UserActionHandler {
                override fun onUserAction() {
                    sharedPrefsClient.setTripStatus(false)
                    navigationHandler.navigateTo(ACTION_HOME)
                }
            }
            userAlertClient.showDialogMessage("Trip Completed","All packets delivered.",handler)
        }
    }
}