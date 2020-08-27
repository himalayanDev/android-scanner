package com.myfav.rider.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Log
import com.myfav.rider.ui.home.data.PacketInfo
import com.myfav.rider.ui.home.data.TripDetails
import org.json.JSONObject

class SharedPrefsClient(context: Context?) {

    private lateinit var mEditor: Editor
    private lateinit var mPrefs: SharedPreferences
    private val PREFS_NAME = "myfav-rider"

    init {
        if (context != null) {
            mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            mEditor = mPrefs.edit()
        }
    }

    fun clearAllData() {
        mEditor.clear()
        mEditor.apply()
    }

    fun setTripStatus(status: Boolean){
        mEditor.putBoolean("TripStatus", status)
        mEditor.apply()
    }

    fun getTripStatus(): Boolean{
        return mPrefs.getBoolean("TripStatus",false)
    }

    fun setTripDetails(response: String){
        Log.i("setTripDetails",""+response)
        mEditor.putString("TripDetails", response)
        mEditor.apply()
    }

    fun getTripDetails(): TripDetails{
        var response =  mPrefs.getString("TripDetails","{}")
        var root = JSONObject(response)
        var tripDetails = TripDetails()
        tripDetails.packetList = arrayListOf()
        var packetInfo = PacketInfo("","","","","")
        for(i in 0 until root.getJSONArray("packets").length()){
            var jo = root.getJSONArray("packets").getJSONObject(i)
            packetInfo = PacketInfo(jo.getString("packet_id"),jo.getString("address"),jo.getString("customer_name"),
                    jo.getString("customer_number"),jo.getString("status"))
            tripDetails.packetList.add(packetInfo)
        }
        tripDetails.tripId = root.getString("trip_id")
        tripDetails.tripStartTime = root.getString("start_time")
        return tripDetails
    }

    fun updatePacket(packetInfo: PacketInfo, index: Int){
        var response =  mPrefs.getString("TripDetails","{}")
        var root = JSONObject(response)
        var tripDetails = TripDetails()
        tripDetails.packetList = arrayListOf()
        for(i in 0 until root.getJSONArray("packets").length()){
            if(i == index){
                var jo = root.getJSONArray("packets").getJSONObject(i)
                jo.put("packet_id", packetInfo.id)
                jo.put("address", packetInfo.address)
                jo.put("customer_name", packetInfo.customerName)
                jo.put("customer_number", packetInfo.customerNumber)
                jo.put("status", packetInfo.status)
            }
        }
        setTripDetails(root.toString())
    }


//    fun saveUserDetails(userProfile: UserProfile) {
//        userProfile.organisation.client
//        mEditor.putString("userProfile.role", UserProfile.getRoleName(userProfile.role))
//        mEditor.putString("organisation.name", userProfile.organisation.name)
//        mEditor.putString("organisation.client", userProfile.organisation.client)
//        mEditor.putString("user.name", userProfile.user.name)
//        mEditor.putString("user.gender", userProfile.user.gender)
//        mEditor.putString("user.address", userProfile.user.address)
//        mEditor.putString("communication.email", userProfile.communication.email)
//        mEditor.putString("communication.phoneNumber", userProfile.communication.phoneNumber)
//        mEditor.apply()
//    }
//
//    fun getUserDetails(): UserProfile {
//        var userProfile = UserProfile()
//        userProfile.role = UserProfile.getRole(mPrefs.getString("userProfile.role","") ?: "")
//        userProfile.organisation.name = mPrefs.getString("organisation.name","") ?: ""
//        userProfile.organisation.client = mPrefs.getString("organisation.client","") ?: ""
//        userProfile.user.name = mPrefs.getString("user.name","") ?: ""
//        userProfile.user.gender = mPrefs.getString("user.gender","") ?: ""
//        userProfile.user.address = mPrefs.getString("user.address","") ?: ""
//        userProfile.communication.email = mPrefs.getString("communication.email","") ?: ""
//        userProfile.communication.phoneNumber = mPrefs.getString("communication.phoneNumber","") ?: ""
//        return userProfile
//    }
}