package com.myfav.rider.ui.login.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.myfav.rider.databinding.LoginBinding
import com.myfav.rider.interfaces.NavigationHandler
import com.myfav.rider.ui.home.HomeActivity
import com.myfav.rider.ui.login.LoginViewModel
import com.myfav.rider.ui.login.data.LoginError
import com.myfav.rider.ui.login.interfaces.LoginResultHandler
import com.myfav.rider.utils.UserAlertClient

class Login : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: LoginBinding
    private lateinit var userAlertClient: UserAlertClient
    private lateinit var navigationHandler: NavigationHandler

    companion object {
        private var INSTANCE: Login? = null

        fun getInstance(): Login {
            return INSTANCE ?: Login()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationHandler) {
            navigationHandler = context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement NavigationHandler"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginBinding.inflate(layoutInflater)
        init()
        return binding.root
    }


    private fun init() {
        userAlertClient = UserAlertClient(activity)
        viewModel = ViewModelProviders.of(requireActivity()).get(LoginViewModel::class.java)

        binding.login.setOnClickListener {
            userAlertClient.showWaitDialog("Authenticating...")
            viewModel.validateLoginForm(
                binding.username.text.toString(),
                binding.password.text.toString(), loginResultHandler
            )
        }
    }

    private var loginResultHandler : LoginResultHandler = object : LoginResultHandler{
        override fun onSuccess() {
            userAlertClient.closeWaitDialog()
            var intent = Intent(context, HomeActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        override fun onValidationError(errorList: MutableList<LoginError>?) {
            userAlertClient.closeWaitDialog()
            if (errorList != null) {
                for(err in  errorList){
                    if(err.type == 0){
                        binding.usernameErr.text = err.message
                    }
                    if(err.type == 1){
                        binding.passwordErr.text = err.message
                    }
                }
            }
        }

        override fun onServerError(msg: String) {
            userAlertClient.closeWaitDialog()
            userAlertClient.showDialogMessage("Error Alert",msg,false)
        }

    }

}
