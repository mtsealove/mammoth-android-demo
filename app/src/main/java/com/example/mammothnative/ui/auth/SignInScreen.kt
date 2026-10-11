package com.example.mammothnative.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mammothnative.R
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

private val SignInBackground = Color(0xFF4A3730)

/**
 * 웹 SignInActivity.
 * 카카오·애플 로그인은 아직 옮기지 않아 휴대폰 번호 로그인만 둔다.
 */
@Composable
fun SignInScreen(
    onBack: () -> Unit,
    onPhoneSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SignInBackground),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_left),
            contentDescription = "뒤로",
            tint = MammothTheme.colors.icon.reverse,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(24.dp)
                .clickableNoRipple(onClick = onBack),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mammoth_wordmark),
                contentDescription = "매머드커피",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxWidth(0.6f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MammothButton(
                text = "휴대폰 번호로 계속하기",
                onClick = onPhoneSignInClick,
                size = MammothButtonSize.Lg,
                variant = MammothButtonVariant.Disabled,
                fullWidth = true,
            )
            MammothText(
                text = "로그인 없이 둘러보기",
                color = MammothTheme.colors.text.reverse,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickableNoRipple(onClick = onBack),
            )
        }
    }
}
