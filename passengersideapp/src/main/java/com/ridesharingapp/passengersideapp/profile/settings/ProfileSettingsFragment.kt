package com.ridesharingapp.passengersideapp.profile.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.zhuinden.simplestackextensions.fragmentsktx.lookup

class ProfileSettingsFragment : Fragment() {

    private val viewModel by lazy { lookup<ProfileSettingsViewModel>()}
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            // Dispose the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ProfileSettingsScreen(viewModel, viewModel.isUserRegistered())
            }
        }
    }
}