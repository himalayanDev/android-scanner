package com.myfav.rider.ui.home

import android.app.Application
import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myfav.rider.interfaces.PacketDetailsRequestHandler
import com.myfav.rider.ui.home.data.PacketInfo
import com.myfav.rider.ui.home.data.UserSelection
import com.myfav.rider.utils.SharedPrefsClient
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HomeViewModel(application: Application) : AndroidViewModel(application)  {

    private lateinit var processingPacket: PacketInfo
    private var context: Context = application.applicationContext
    private val _tag = "_SuperAdminViewModel"
    private val _currentFragmentPointer = MutableLiveData<Int>(ACTION_HOME)
    val currentFragmentPointer: LiveData<Int> = _currentFragmentPointer
    private lateinit var sharedPrefsClient: SharedPrefsClient

    init {
        sharedPrefsClient = SharedPrefsClient(context)
    }

    fun setFragment (fragmentIndex: Int){
        _currentFragmentPointer.value = fragmentIndex
    }

    fun setProcessingPacket (packetInfo: PacketInfo){
        processingPacket = packetInfo
    }

    fun getProcessingPacket () : PacketInfo{
        return processingPacket;
    }

    fun markPacketDelivered () {
        var tripDetails = sharedPrefsClient.getTripDetails()
        var index = 0;
        var selectedIndex = -1
        for(packet in tripDetails.packetList){
            if(packet.id.compareTo(getProcessingPacket().id)==0){
                packet.status = "Delivered"
                selectedIndex = index
            }
            index++
        }

        Log.i("markPacketDelivered",tripDetails.packetList[selectedIndex].status)
        sharedPrefsClient.updatePacket(tripDetails.packetList[selectedIndex],selectedIndex)
    }

    //HOME
    fun getSelectionList(): ArrayList<UserSelection> {
        var list = ArrayList<UserSelection>()
        list.add(UserSelection("Create Trip", ACTION_CREATE_TRIP,false))
        list.add(UserSelection("Resume Trip", ACTION_RESUME_TRIP,false))
        list.add(UserSelection("Logout", ACTION_LOGOUT,false))
        return list
    }

    private fun getPacketList(barcodeList: ArrayList<String>): JSONObject {
        var dummyRootJson = "{\"trip_id\":\"HFT000010\",\"start_time\":\"13:40\",\"packets\":[]}"
        var dummyPacket = "{\"packet_id\":\"MBBX0001191\",\"address\":\"E91 Sector 39, Chandigarh. Landmark: GolfCourse gate# 5\",\"customer_name\":\"Mr Rajneesh Sharma\",\"customer_number\":\"7508902468\",\"status\":\"Out For Delivery\"}"
        var root = JSONObject(dummyRootJson)
        var packet = JSONObject(dummyPacket)

        var list = ArrayList<PacketInfo>()
        for (x in 0 until barcodeList.size){
            packet = JSONObject(dummyPacket)
            packet.put("packet_id",barcodeList[x])
            root.getJSONArray("packets").put(packet)
        }
        return root
    }

    fun getLocalPacketList() : ArrayList<PacketInfo>{
        return sharedPrefsClient.getTripDetails().packetList
    }

    lateinit var returnCallback:Runnable
    public fun loadPacketList(list: ArrayList<String>,packetDetailsRequestHandler: PacketDetailsRequestHandler) {
        Thread(Runnable {
            val handler = Handler(context.mainLooper)
            Timer().schedule(2000) {
                returnCallback = Runnable {
                    packetDetailsRequestHandler.onResult(getPacketList(list))
                }
                handler.post(returnCallback!!)
            }
        }).start()
    }


    companion object {
        const val ACTION_HOME = 0
        const val ACTION_CREATE_TRIP = 1
        const val ACTION_RESUME_TRIP = 2
        const val ACTION_LOGOUT = 3
        const val ACTION_SCAN_DELIVERY = 4
    }
}
