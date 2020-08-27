package com.myfav.rider.ui.home.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.myfav.rider.databinding.HomeBinding
import com.myfav.rider.ui.home.HomeViewModel
import com.myfav.rider.ui.home.adapters.UserSelectionActionAdapter
import com.myfav.rider.ui.home.bottomsheet.PacketDetails
import com.myfav.rider.utils.UserAlertClient


class Home : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: HomeBinding
    private lateinit var userAlertClient: UserAlertClient
    private lateinit var gridAdapter: UserSelectionActionAdapter

    companion object {
        private var INSTANCE: Home? = null

        fun getInstance(): Home {
            return INSTANCE ?: Home()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = HomeBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        userAlertClient = UserAlertClient(activity)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        binding.selectionGrid.layoutManager = GridLayoutManager(context, 1)
        gridAdapter = UserSelectionActionAdapter(
            context,
            viewModel.getSelectionList(),
            userSelectionListener
        )
        binding.selectionGrid.adapter = gridAdapter
    }

    private var userSelectionListener = UserSelectionActionAdapter.UserSelectionListener {
        Log.i("_UserSelectionListener", "pos: $it")
        viewModel.setFragment(viewModel.getSelectionList()[it].Action)

//        var adminClient = AdministrationClient()
//        adminClient.createUser(context)
    }

}