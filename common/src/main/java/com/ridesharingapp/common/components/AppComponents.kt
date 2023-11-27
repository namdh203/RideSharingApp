package com.ridesharingapp.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridesharingapp.common.R
import com.ridesharingapp.common.ui.theme.BgColor
import com.ridesharingapp.common.ui.theme.GrayColor
import com.ridesharingapp.common.ui.theme.Primary
import com.ridesharingapp.common.ui.theme.Secondary
import com.ridesharingapp.common.ui.theme.TextColor
import com.ridesharingapp.common.ui.theme.componentShapes

@Composable
fun NormalTextComponent(value: String, modifier: Modifier = Modifier) {
    Text(
        text = value,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        color = TextColor,
        textAlign = TextAlign.Center
    )
}

@Composable
fun HeadingTextComponent(value: String, modifier: Modifier = Modifier) {
    Text(
        text = value,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        color = TextColor,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldComponent(
    labelValue: String,
    painterResource: Painter,
    onTextChange: (String) -> Unit,
    textValue: String,
    errorMessage: String = "",
    errorStatus: Boolean = false,
    isEmail: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgColor)
            .clip(componentShapes.small),
        label = { Text(text = labelValue) },
        value = textValue,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = colorResource(id = R.color.colorPrimary),
            focusedLabelColor = Primary,
            cursorColor = Primary,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isEmail) KeyboardType.Email else KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        onValueChange = {
            onTextChange(it)
        },
        leadingIcon = {
            Icon(
                painter = painterResource,
                contentDescription = "profile icon"
            )
        },
        singleLine = true,
        isError = errorStatus
    )

    if (errorStatus) {
        ErrorText(errorMessage = errorMessage)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextFieldComponent(
    labelValue: String,
    painterResource: Painter,
    onTextChange: (String) -> Unit,
    password: String,
    errorStatus: Boolean
) {
    val localFocusManager = LocalFocusManager.current
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgColor)
            .clip(componentShapes.small),
        label = { Text(text = labelValue) },
        value = password,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = colorResource(id = R.color.colorPrimary),
            focusedLabelColor = Primary,
            cursorColor = Primary,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions{
            localFocusManager.clearFocus()
        },
        singleLine = true,
        onValueChange = {
            onTextChange(it)
        },
        leadingIcon = {
            Icon(
                painter = painterResource,
                contentDescription = "profile icon"
            )
        },
        trailingIcon = {
            val iconImage = if (passwordVisible) {
                Icons.Filled.VisibilityOff
            } else {
                Icons.Filled.Visibility
            }

            val description = if (passwordVisible) {
                stringResource(id = R.string.hide_password)
            } else {
                stringResource(id = R.string.show_password)
            }

            IconButton(onClick = {
                passwordVisible = !passwordVisible
            }) {
                Icon(imageVector = iconImage, contentDescription = description)
            }
        },
        visualTransformation =
        if (passwordVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        isError = errorStatus
    )

    if (errorStatus) {
        ErrorText(errorMessage = stringResource(id = R.string.password_format_error_message))
    }
}

@Composable
fun CheckboxComponent(label: @Composable () -> Unit, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var checked by rememberSaveable {
            mutableStateOf(false)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange.invoke(it)
            }
        )

        label()
    }

}

@Composable
fun TermsAndConditionsText(onTextClickAction: (String) -> Unit) {
    val initialText = "By continuing you accept our "
    val privacyText = "Privacy Policy"
    val andText = " and "
    val termsText = "Terms of Use"

    val annotatedString = buildAnnotatedString {
        append(initialText)
        withStyle(style = SpanStyle(color = Primary)) {
            pushStringAnnotation(
                tag = privacyText,
                annotation = privacyText
            )
            append(privacyText)
        }
        append(andText)
        withStyle(style = SpanStyle(color = Primary)) {
            pushStringAnnotation(
                tag = termsText,
                annotation = termsText
            )
            append(termsText)
        }
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString
                .getStringAnnotations(offset, offset + 1)
                .firstOrNull()?.also {
                    if (it.item == privacyText || it.item == termsText)
                        onTextClickAction(it.item)
                }
        }
    )
}

@Composable
fun ButtonComponent(labelValue: String, onClickAction: () -> Unit, isEnabled: Boolean = true) {
    Button(
        onClick = onClickAction,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        enabled = isEnabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(48.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(Secondary, Primary))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = labelValue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DividerTextComponent() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = GrayColor,
            thickness = 1.dp
        )
        Text(
            text = "or",
            fontSize = 18.sp,
            color = TextColor,
            modifier = Modifier.padding(8.dp)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = GrayColor,
            thickness = 1.dp
        )
    }
}

@Composable
fun RegisterLoginRoutingText(tryingToLogin: Boolean, onTextClickAction: (String) -> Unit) {
    val promptText =
        if (tryingToLogin) stringResource(id = R.string.go_to_login)
        else stringResource(id = R.string.not_have_account)
    val switchText =
        if (tryingToLogin) stringResource(id = R.string.login)
        else stringResource(id = R.string.register)

    val annotatedString = buildAnnotatedString {
        append(promptText)
        withStyle(style = SpanStyle(color = Primary)) {
            pushStringAnnotation(
                tag = switchText,
                annotation = switchText
            )
            append(switchText)
        }
    }

    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 21.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center
        ),
        text = annotatedString,
        onClick = { offset ->
            annotatedString
                .getStringAnnotations(offset, offset + 1)
                .firstOrNull()?.also {
                    if (it.item == switchText)
                        onTextClickAction(it.item)
                }
        }
    )
}

@Composable
fun UnderlinedClickableText(value: String, onClick: () -> Unit) {
    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center,
            color = GrayColor,
            textDecoration = TextDecoration.Underline
        ),
        text = AnnotatedString(
            text = value
        ),
        onClick = { onClick() }
    )
}

@Composable
fun ErrorText(errorMessage: String) {
    Text(
        text = errorMessage,
        color = Color.Red,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun UserAuthenticationFailedAlertDialog(
    message: String = stringResource(R.string.incorrect_email_or_password_please_try_again),
    dismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { dismiss() },
        confirmButton = {
            TextButton(onClick = { dismiss() }) {
                Text(text = "OK")
            }
        },
        title = { Text(text = "Error")},
        text = { Text(text = message) }
    )
}