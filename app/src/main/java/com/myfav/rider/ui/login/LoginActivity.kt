package sukriti.ngo.mis.ui.login

import android.os.Build
import com.myfav.rider.utils.UserAlertClient
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.myfav.rider.databinding.LoginActivityBinding
import com.myfav.rider.interfaces.NavigationHandler
import com.myfav.rider.ui.login.Fragments.Login
import com.myfav.rider.ui.login.LoginViewModel
import com.myfav.rider.utils.NavigationClient


class LoginActivity : NavigationHandler,AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: LoginActivityBinding
    private lateinit var userAlertClient: UserAlertClient
    private lateinit var navigationClient: NavigationClient

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        navigationClient = NavigationClient(supportFragmentManager)
        userAlertClient = UserAlertClient(this)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        navigationClient.loadFragment(Login.getInstance(),"login", false)
    }

    override fun navigateTo(navigationAction: Int) {
        TODO("Not yet implemented")
    }

}
