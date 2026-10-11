package com.example.mammothnative.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothSpacing
import com.example.mammothnative.ui.theme.MammothTheme

/**
 * 웹 MenuFragment 중 사용자 정보 영역.
 * 나머지 메뉴 목록·푸터는 아직 옮기지 않았다.
 */
@Composable
fun MenuScreen(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile.dataOrNull

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MammothTheme.colors.surface.background.bg1)
            .statusBarsPadding()
            .padding(MammothSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MammothSpacing.lg),
    ) {
        MammothText(
            text = if (uiState.isSignedIn) "${profile?.nickname ?: "고객"}님" else "전체메뉴",
            style = MammothTheme.typography.headline2,
            fontWeight = FontWeight.Bold,
        )

        if (!uiState.isSignedIn) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MammothShapes.md)
                    .background(MammothTheme.colors.surface.button.unselected)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MammothText(text = "로그인 후 확인할 수 있어요.")
                MammothButton(
                    text = "로그인 하러 가기",
                    onClick = onSignInClick,
                    size = MammothButtonSize.Xs,
                )
            }
            return@Column
        }

        when (val state = uiState.profile) {
            is Loadable.Loading -> MammothText(
                text = "불러오는 중...",
                color = MammothTheme.colors.text.secondary,
            )

            is Loadable.Failure -> MammothText(
                text = state.throwable.message ?: "정보를 불러오지 못했어요",
                color = MammothTheme.colors.text.error,
            )

            is Loadable.Success -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MammothShapes.md)
                    .background(MammothTheme.colors.surface.button.unselected)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AssetItem(label = "쿠폰", value = "${state.data.couponCount}개")
                AssetItem(label = "스탬프", value = "${state.data.stampCount}개")
                AssetItem(label = "포인트", value = "%,d원".format(state.data.points))
            }
        }

        MammothButton(
            text = "로그아웃",
            onClick = viewModel::signOut,
            size = MammothButtonSize.Sm,
            variant = MammothButtonVariant.Line,
            fullWidth = true,
        )
    }
}

@Composable
private fun AssetItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MammothText(
            text = label,
            style = MammothTheme.typography.label,
            color = MammothTheme.colors.text.secondary,
        )
        MammothText(text = value, fontWeight = FontWeight.Bold)
    }
}
