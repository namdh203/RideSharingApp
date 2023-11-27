package com.ridesharingapp.passengersideapp.navigation

import androidx.fragment.app.Fragment
import com.ridesharingapp.passengersideapp.authentication.login.LoginFragment
import com.ridesharingapp.passengersideapp.authentication.login.LoginViewModel
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentKey
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginKey(private val noArgsPlaceholder: String = ""): DefaultFragmentKey(),
    DefaultServiceProvider.HasServices {
    override fun instantiateFragment(): Fragment = LoginFragment()

    override fun getScopeTag(): String = toString()

    //How to create a scoped service
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(LoginViewModel(backstack, lookup(), lookup(), lookup()))
        }
    }
}