package com.example.mammothnative.ui.items

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mammothnative.R
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.domain.model.ItemDetail
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonShape
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.items.component.OptionPanel
import com.example.mammothnative.ui.items.component.PersonalOptionSheet
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple
import com.example.mammothnative.ui.util.toPriceText

/**
 * 웹 ItemActivity.
 *
 * 옵션은 웹과 같은 단계식이다. 처음에는 첫 옵션 그룹만 하단에 보이고,
 * 옵션을 고르면 패널이 위로 올라오면서 나머지 그룹이 드러난다.
 * 기본 옵션을 다 고르면 '다음' 자리에 주문 버튼이 올라오고 퍼스널 옵션 카드가 나타난다.
 */
@Composable
fun ItemScreen(
    onBack: () -> Unit,
    onSignInRequired: () -> Unit,
    onAddedToCart: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ItemEvent.AddedToCart -> {
                    // 화면을 바로 닫으므로 스낵바 대신 Toast 로 알린다(웹의 toast 와 같은 역할).
                    Toast.makeText(context, "장바구니에 추가했어요.", Toast.LENGTH_SHORT).show()
                    onAddedToCart()
                }

                is ItemEvent.RequireSignIn -> onSignInRequired()
                is ItemEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MammothTheme.colors.surface.background.bg1,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val panelMaxHeight = maxHeight * 0.6f

            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                when (val detail = uiState.detail) {
                    is Loadable.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MammothTheme.colors.primary)
                    }

                    is Loadable.Failure -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MammothText(
                            text = detail.throwable.message ?: "메뉴를 불러오지 못했어요",
                            color = MammothTheme.colors.text.secondary,
                            textAlign = TextAlign.Center,
                        )
                    }

                    is Loadable.Success -> ItemInfo(
                        item = detail.data,
                        price = detail.data.priceFor(uiState.orderType),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (uiState.item != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                ) {
                    OptionPanel(
                        uiState = uiState,
                        maxHeight = panelMaxHeight,
                        onOptionClick = viewModel::onOptionClick,
                        onCardClick = viewModel::onOptionCardClick,
                        onPersonalClick = viewModel::openPersonalSheet,
                    )
                    QuantityRow(
                        quantity = uiState.quantity,
                        totalPrice = uiState.totalPrice,
                        onQuantityChange = viewModel::onQuantityChange,
                    )
                    BottomActions(
                        uiState = uiState,
                        onNextClick = viewModel::onNextClick,
                        onAddToCart = viewModel::addToCart,
                        onOrderClick = {
                            Toast.makeText(context, "주문서 화면은 아직 없어요.", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }

    if (uiState.isPersonalSheetOpen) {
        PersonalOptionSheet(
            uiState = uiState,
            onDismiss = viewModel::closePersonalSheet,
            onGroupSelect = viewModel::onPersonalGroupSelect,
            onOptionClick = viewModel::onPersonalOptionClick,
            onQuantityChange = viewModel::onPersonalQuantityChange,
            onApply = viewModel::applyPersonalOptions,
        )
    }
}

@Composable
private fun ItemInfo(
    item: ItemDetail,
    price: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(MammothShapes.md)
                .background(MammothTheme.colors.surface.background.bg2),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MammothText(
                text = item.name,
                style = MammothTheme.typography.headline,
                fontWeight = FontWeight.Bold,
            )
            item.description?.let {
                MammothText(
                    text = it,
                    style = MammothTheme.typography.label,
                    fontWeight = FontWeight.Medium,
                    color = MammothTheme.colors.text.secondary,
                )
            }
            MammothText(
                text = price.toPriceText(),
                style = MammothTheme.typography.title,
                fontWeight = FontWeight.Bold,
            )
            item.warnings?.let { warnings ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MammothShapes.md)
                        .background(MammothTheme.colors.surface.background.bg2)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MammothText(
                        text = "유의사항",
                        fontWeight = FontWeight.Medium,
                        color = MammothTheme.colors.text.secondary,
                    )
                    MammothText(
                        text = warnings,
                        style = MammothTheme.typography.label,
                        color = MammothTheme.colors.text.secondary,
                    )
                }
            }
        }
    }
}

/**
 * 웹과 같이 '다음' 버튼 위로 주문 버튼이 밀려 올라온다.
 */
@Composable
private fun BottomActions(
    uiState: ItemUiState,
    onNextClick: () -> Unit,
    onAddToCart: () -> Unit,
    onOrderClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        MammothButton(
            text = "다음",
            onClick = onNextClick,
            size = MammothButtonSize.Lg,
            shape = MammothButtonShape.Square,
            variant = if (uiState.isNextDisabled) {
                MammothButtonVariant.Disabled
            } else {
                MammothButtonVariant.Filled
            },
            fullWidth = true,
            isCta = true,
        )

        AnimatedVisibility(
            visible = uiState.isOrderBarVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MammothTheme.colors.surface.background.bg1),
            ) {
                MammothButton(
                    text = "장바구니 담기",
                    onClick = onAddToCart,
                    size = MammothButtonSize.Lg,
                    shape = MammothButtonShape.Square,
                    variant = MammothButtonVariant.Secondary,
                    enabled = uiState.canAddToCart,
                    fullWidth = true,
                    isCta = true,
                    modifier = Modifier.weight(1f),
                )
                MammothButton(
                    text = "주문하기",
                    onClick = onOrderClick,
                    size = MammothButtonSize.Lg,
                    shape = MammothButtonShape.Square,
                    enabled = uiState.canAddToCart,
                    fullWidth = true,
                    isCta = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 웹 AmountRow: 수량 조절 + 합계 금액 */
@Composable
private fun QuantityRow(
    quantity: Int,
    totalPrice: Int,
    onQuantityChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MammothTheme.colors.surface.background.bg1)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuantityButton(
                iconRes = R.drawable.ic_minus,
                description = "수량 줄이기",
                onClick = { onQuantityChange(-1) },
            )
            MammothText(
                text = "$quantity",
                style = MammothTheme.typography.title,
                fontWeight = FontWeight.Bold,
            )
            QuantityButton(
                iconRes = R.drawable.ic_plus,
                description = "수량 늘리기",
                onClick = { onQuantityChange(1) },
            )
        }
        MammothText(
            text = totalPrice.toPriceText(),
            style = MammothTheme.typography.headline2,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QuantityButton(iconRes: Int, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(MammothBorderWidth, MammothTheme.colors.surface.border.default, CircleShape)
            .clickableNoRipple(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = MammothTheme.colors.icon.default,
            modifier = Modifier.size(16.dp),
        )
    }
}
