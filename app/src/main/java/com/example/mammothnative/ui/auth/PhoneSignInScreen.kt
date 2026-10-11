package com.example.mammothnative.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mammothnative.R
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonShape
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.component.MammothTextField
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

/**
 * 웹 SignInWithPhoneActivity.
 * 번호 입력 → 인증번호 발송 → 6자리 확인 → 로그인.
 */
@Composable
fun PhoneSignInScreen(
    onBack: () -> Unit,
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneSignInViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PhoneSignInEvent.SignedIn -> onSignedIn()
                is PhoneSignInEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MammothTheme.colors.surface.background.bg1,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MammothButton(
                text = "다음",
                onClick = viewModel::submit,
                size = MammothButtonSize.Lg,
                shape = MammothButtonShape.Square,
                fullWidth = true,
                isCta = true,
                enabled = uiState.canSubmit,
                modifier = Modifier.imePadding(),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = "뒤로",
                tint = MammothTheme.colors.icon.default,
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .clickableNoRipple(onClick = onBack),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MammothText(
                    text = "휴대폰 번호로 시작하기",
                    style = MammothTheme.typography.title,
                    fontWeight = FontWeight.Bold,
                )
                MammothText(
                    text = "회원가입 및 로그인 진행을 위해\n휴대폰 번호 인증을 완료해 주세요.",
                    fontWeight = FontWeight.Medium,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MammothTextField(
                    value = uiState.formattedPhoneNo,
                    onValueChange = viewModel::onPhoneChange,
                    placeholder = "휴대폰 번호를 입력해 주세요",
                    keyboardType = KeyboardType.Number,
                    trailing = {
                        MammothButton(
                            text = if (uiState.isSmsSent) "재발송" else "인증하기",
                            onClick = viewModel::sendSms,
                            size = MammothButtonSize.Xs,
                            // 쿨다운 중에는 눌리지 않지만 눌린 듯 보이지 않게 비활성 모양으로 둔다(웹과 동일).
                            variant = if (uiState.resendSecondsLeft > 0) {
                                MammothButtonVariant.Disabled
                            } else {
                                MammothButtonVariant.Filled
                            },
                            enabled = uiState.canSendSms,
                        )
                    },
                )

                if (uiState.isSmsSent) {
                    MammothTextField(
                        value = uiState.code,
                        onValueChange = viewModel::onCodeChange,
                        placeholder = "인증번호 6자리를 입력해 주세요",
                        keyboardType = KeyboardType.Number,
                        errorText = uiState.errorText,
                        trailing = {
                            MammothText(
                                text = uiState.codeTimerText,
                                style = MammothTheme.typography.label,
                                fontWeight = FontWeight.Medium,
                                color = if (uiState.errorText != null) {
                                    MammothTheme.colors.text.error
                                } else {
                                    MammothTheme.colors.text.link
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}
