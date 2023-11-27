package com.ridesharingapp.driversideapp.navigation

import androidx.fragment.app.Fragment
import com.ridesharingapp.driversideapp.home.HomeFragment
import com.ridesharingapp.driversideapp.home.HomeViewModel
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentKey
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import kotlinx.parcelize.Parcelize

@Parcelize
data class DriverHomeKey(private val noArgsPlaceholder: String = "") : DefaultFragmentKey() ,
    DefaultServiceProvider.HasServices {
    override fun instantiateFragment(): Fragment = HomeFragment()

    override fun getScopeTag(): String = toString()

    //How to create a scoped service
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(HomeViewModel(backstack, lookup(), lookup()))
        }
    }

}
