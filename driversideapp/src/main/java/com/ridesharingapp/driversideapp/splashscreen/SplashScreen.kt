package com.ridesharingapp.driversideapp.splashscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ridesharingapp.common.R
import com.ridesharingapp.common.style.color_primary
import com.ridesharingapp.common.style.color_white
import com.ridesharingapp.common.style.typography


@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .background(color = color_primary)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = typography.h1.copy(color = color_white)
        )
        Text(
            text = stringResource(id = R.string.need_a_ride),
            style = typography.subtitle2.copy(color = color_white)
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview
@Composable
fun PreviewSplashScreen() {
    SplashScreen()
}