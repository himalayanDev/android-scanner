package com.myfav.rider.ui.login

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import com.myfav.rider.ui.login.data.LoginError
import com.myfav.rider.ui.login.interfaces.LoginResultHandler
import java.util.*
import kotlin.concurrent.schedule


class LoginViewModel(application: Application) : AndroidViewModel(application) {


    private var context: Context = application.applicationContext

    //LIVE DATA
//    private val _loginForm = MutableLiveData<LoginFormState>()
//    val loginFormState: LiveData<LoginFormState> = _loginForm
//    fun clearLoginExecutionState() {
//        _LoginExecutionState.value = ExecutionState()
//    }


    companion object {
        const val NAV_ACTION_LOGIN = 0
        const val NAV_ACTION_NEW_USER = 1
        const val NAV_ACTION_FORGOT_PASSWORD = 2
    }

    init {

    }


    fun validateLoginForm(username: String, password: String, loginResultHandler: LoginResultHandler) {
        var errList: MutableList<LoginError> = mutableListOf<LoginError>()
        var err =  LoginError()

        if(username.isNullOrEmpty()){
            err.message = "Username Required. Please enter user-name."
            err.type = 0
            errList.add(err)
        }

        if(password.isNullOrEmpty()){
            err.message = "Password Required. Please enter user-name."
            err.type = 1
            errList.add(err)
        }

        if(errList.size==0){
            authenticate(loginResultHandler)
        }else{
            loginResultHandler.onValidationError(errList)
        }
    }

    private fun authenticate(loginResultHandler: LoginResultHandler) {
        Timer().schedule(2000) {
            loginResultHandler.onSuccess()
        }
    }


}
