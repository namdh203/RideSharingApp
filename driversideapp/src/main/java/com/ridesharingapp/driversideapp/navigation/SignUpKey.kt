package com.ridesharingapp.driversideapp.navigation

import androidx.fragment.app.Fragment
import com.ridesharingapp.driversideapp.authentication.signup.SignUpFragment
import com.ridesharingapp.driversideapp.authentication.signup.SignUpViewModel
import com.zhuinden.simplestack.ServiceBinder

import com.zhuinden.simplestackextensions.fragments.DefaultFragmentKey
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignUpKey(private val isFromLogin: Boolean = false) : DefaultFragmentKey(),
    DefaultServiceProvider.HasServices {
    override fun instantiateFragment(): Fragment = SignUpFragment()

    override fun getScopeTag(): String = toString()

    //How to create a scoped service
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(SignUpViewModel(backstack, lookup()))
        }
    }
}