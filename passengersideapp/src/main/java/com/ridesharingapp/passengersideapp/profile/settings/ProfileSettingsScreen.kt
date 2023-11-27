package com.ridesharingapp.passengersideapp.profile.settings

import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ridesharingapp.common.R
import com.ridesharingapp.common.domain.GrabLamUser
import com.ridesharingapp.common.style.color_black
import com.ridesharingapp.common.style.color_primary
import com.ridesharingapp.common.style.color_white
import com.ridesharingapp.common.style.typography
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun ProfileSettingsScreen(
    viewModel: ProfileSettingsViewModel,
    unregisteredUserView: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color_white),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ProfileToolbar(viewModel = viewModel)

        var driverSwitchState by rememberSaveable {
            mutableStateOf(false)
        }

        val user by viewModel.userModel.collectAsState()

        ProfileHeader(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 64.dp),
            viewModel = viewModel,
            user = user
        )

        UserTypeState(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight()
                .border(
                    width = 1.dp,
                    color_black.copy(alpha = 0.12f),
                    RoundedCornerShape(4.dp)
                ),
            viewModel = viewModel,
            user = user
        )
    }
}

@Composable
fun ProfileToolbar(
    modifier: Modifier = Modifier,
    viewModel: ProfileSettingsViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.clickable { viewModel.handleBackPress() },
            imageVector = Icons.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(id = R.string.close_icon)
        )

        TextButton(
            onClick = { viewModel.handleLogOut() }
        ) {
            Text(
                text = stringResource(id = R.string.log_out),
                style = typography.button.copy(
                    color = color_black,
                    fontWeight = FontWeight.Light
                )
            )
        }
    }
}

@Composable
fun ProfileHeader(
    modifier: Modifier,
    viewModel: ProfileSettingsViewModel,
    user: GrabLamUser?
) {

    //Note: You would want to do better null checking than this in a prod app
    if (user != null) Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(modifier = Modifier, viewModel = viewModel, user = user)
        Text(
            modifier = Modifier
                .padding(start = 16.dp),
            text = user.username,
            style = typography.h2
        )
    }
}

@Composable
fun ProfileAvatar(
    modifier: Modifier,
    viewModel: ProfileSettingsViewModel,
    user: GrabLamUser
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {

        if (user.avatarPhotoUrl == "") Image(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .alpha(0.86f),
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_account_circle_24),
            contentDescription = stringResource(id = R.string.user_avatar),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(color_primary)
        ) else GlideImage(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape),
            imageModel = { user.avatarPhotoUrl }
        )

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                viewModel.handleThumbnailUpdate(it.data?.data)
            }
        )

        Icon(
            modifier = Modifier.clickable {
                launcher.launch(
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                )
            },
            imageVector = ImageVector.vectorResource(id = R.drawable.check_circle_24px),
            contentDescription = stringResource(id = R.string.edit_avatar),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun UserTypeState(
    modifier: Modifier,
    viewModel: ProfileSettingsViewModel,
    user: GrabLamUser?
) {
    if (user != null) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Top)
                    .padding(top = 16.dp),
                text = if (user.type != com.ridesharingapp.common.domain.UserType.PASSENGER.value) stringResource(id = R.string.driver)
                else stringResource(id = R.string.passenger),
                style = typography.h3
            )

            Switch(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 16.dp)
                    .scale(1.5f),
                checked = user.type != com.ridesharingapp.common.domain.UserType.PASSENGER.value,
                onCheckedChange = { viewModel.handleToggleUserType() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = color_primary,
                    checkedTrackColor = color_primary
                )
            )
        }

    }
}