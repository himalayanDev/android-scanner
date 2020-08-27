package com.myfav.rider.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.myfav.rider.R
import com.myfav.rider.databinding.ActivityHomeBinding
import com.myfav.rider.interfaces.*
import com.myfav.rider.ui.home.HomeViewModel.Companion.ACTION_CREATE_TRIP
import com.myfav.rider.ui.home.HomeViewModel.Companion.ACTION_HOME
import com.myfav.rider.ui.home.HomeViewModel.Companion.ACTION_RESUME_TRIP
import com.myfav.rider.ui.home.HomeViewModel.Companion.ACTION_SCAN_DELIVERY
import com.myfav.rider.ui.home.bottomsheet.PacketDetails
import com.myfav.rider.ui.home.data.PacketInfo
import com.myfav.rider.ui.home.fragments.DeliveryScanner
import com.myfav.rider.ui.home.fragments.Home
import com.myfav.rider.ui.home.fragments.Scanner
import com.myfav.rider.ui.home.fragments.Trip
import com.myfav.rider.utils.SharedPrefsClient
import com.myfav.rider.utils.UserAlertClient
import sukriti.ngo.mis.ui.login.LoginActivity

class HomeActivity : NavigationHandler, AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityHomeBinding
    private lateinit var userAlertClient: UserAlertClient
    private lateinit var sharedPrefsClient: SharedPrefsClient
    private var barCodes: ArrayList<String> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()

        viewModel.currentFragmentPointer.observe(this, Observer {
            Log.i("_login", "observer")
            val action = it ?: return@Observer
            Log.i("_login", "action: " + action)
            when (action) {
                ACTION_HOME -> {

                }

                ACTION_CREATE_TRIP -> {
                    if(sharedPrefsClient.getTripStatus()){
                        userAlertClient.showDialogMessage("Trip in progress","A trip is already in progress. Choose 'Resume Trip' to proceed",false)
                    }else{
                        navigateTo(ACTION_CREATE_TRIP);
                    }
                }

                HomeViewModel.ACTION_RESUME_TRIP -> {
                    if(sharedPrefsClient.getTripStatus()){
                        navigateTo(ACTION_RESUME_TRIP)
                    }else{
                        userAlertClient.showDialogMessage("No Trip in progress","Choose 'Create Trip' to proceed",false)
                    }
                }

                HomeViewModel.ACTION_LOGOUT -> {
                    sharedPrefsClient.setTripStatus(false)
                    intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                    finish();
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        sharedPrefsClient = SharedPrefsClient(applicationContext)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Action bar and Menu Drawer
        setSupportActionBar(binding.toolbar.mainToolbar)
        var mDrawerToggle = ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar.mainToolbar,
                R.string.Menu,
                R.string.Menu
        )
        binding.drawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
        binding.navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            performAction(item)
            true
        })

        binding.toolbar.action.setOnClickListener(View.OnClickListener {
            if(barCodes.size > 0){
                var handler: UserActionHandler = object : UserActionHandler{
                    override fun onUserAction() {
                        navigateTo(ACTION_RESUME_TRIP)
                    }
                }
                userAlertClient.showDialogMessage("Confirm Action","Would you like to create a trip with scanned packets",handler)
            }
            else{
                userAlertClient.showDialogMessage("No Packets Scanned","No packets scanned. Please scan barcodes to proceed.",false)
            }

        })

        userAlertClient = UserAlertClient(this)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        navigateTo(ACTION_HOME)
    }



    private fun performAction(item: MenuItem) {
        binding.drawerLayout.closeDrawers()
        when (item.itemId) {
            R.id.nav_sign_out -> {

            }

            R.id.nav_profile -> {
                //loadFragment(UserProfileHome.getInstance(), "Profile", "userProfile", true)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun navigateTo(navigationAction: Int) {

        if(navigationAction == ACTION_CREATE_TRIP){
            binding.toolbar.scanContainer.visibility = View.VISIBLE
        }else{
            binding.toolbar.scanContainer.visibility = View.GONE
        }

        when(navigationAction){
            ACTION_HOME ->{
                loadFragment(Home.getInstance(), "Home", "home", true)
            }

            ACTION_SCAN_DELIVERY -> {
                loadFragment(DeliveryScanner.getInstance(), "DeliveryScanner", "DeliveryScanner", true)
            }

            ACTION_CREATE_TRIP ->{
                var ref = Scanner.getInstance()
                ref.setScannerResultHandler(scannerResultHandler)
                ref.setScannerStatusListener(scannerStatusListener)
                loadFragment(ref, "Scanner", "scanner", true)
            }

            ACTION_RESUME_TRIP ->{
                var trip = Trip.getInstance()
                trip.setBarcodeList(barCodes)
                trip.setPacketSelectionHandler(mPacketSelectionHandler)
                loadFragment(trip, "Trip", "trip", true);
            }

        }
    }

    private fun loadFragment(
            fragment: Fragment,
            title: String,
            label: String,
            addToStack: Boolean
    ) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        if (addToStack)
            transaction.addToBackStack(label)
        transaction.commit()
        binding.toolbar.mainToolbarTitle.text = title
    }



    private var scannerResultHandler: ScannerResultHandler = object : ScannerResultHandler {
        override fun doReset() {
            barCodes = arrayListOf()
            binding.toolbar.scanCount.setText("" + barCodes.size)
        }

        override fun onScan(code: String?) {
            if (code != null) {
                barCodes.add(code)
                binding.toolbar.scanCount.setText("" + barCodes.size)
            }
        }
    }

    private var scannerStatusListener: ScannerStatusListener = object : ScannerStatusListener{
        override fun onStatusChanged(isScanning: Boolean) {
            if(isScanning)
                binding.toolbar.action.visibility = View.INVISIBLE
            else
                binding.toolbar.action.visibility = View.VISIBLE
        }

    }

    private var mPacketSelectionHandler: PacketSelectionHandler = object : PacketSelectionHandler {
        override fun onPacketSelected(packet: PacketInfo?, pos: Int) {
            if (packet != null) {
                var  packetDetails = PacketDetails.getInstance()
                packetDetails.setDetails(packet)
                packetDetails.setNavigationHandler(this@HomeActivity)
                packetDetails.apply {
                    show(supportFragmentManager, "PacketDetails")
                }
            }

        }
    }

}
