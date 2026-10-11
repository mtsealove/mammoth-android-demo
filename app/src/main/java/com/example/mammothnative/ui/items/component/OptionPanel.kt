package com.example.mammothnative.ui.items.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mammothnative.domain.model.OptionGroup
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.items.ItemUiState
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple
import com.example.mammothnative.ui.util.toPriceText

private val CardShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
private val CollapsedCardHeight = 66.dp

/**
 * 웹 ItemFooter 의 옵션 영역.
 *
 * 처음에는 첫 옵션 그룹만 보이고, 옵션을 고르면 패널이 위로 늘어나면서 나머지 그룹이 드러난다.
 * 펼쳐진 뒤에는 현재 그룹만 펼쳐지고 나머지는 선택값만 보여주는 줄로 접힌다.
 */
@Composable
fun OptionPanel(
    uiState: ItemUiState,
    maxHeight: Dp,
    onOptionClick: (groupId: String, optionId: String) -> Unit,
    onCardClick: (index: Int) -> Unit,
    onPersonalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = uiState.item?.optionGroups.orEmpty()
    if (groups.isEmpty() && !uiState.isPersonalCardVisible) return

    // 닫힌 상태에서는 첫 그룹만 구성한다. 높이 변화는 animateContentSize 가 이어서 처리한다.
    val visibleGroups = if (uiState.orderBegin) groups else groups.take(1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .animateContentSize(animationSpec = tween(durationMillis = 250)),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            visibleGroups.forEachIndexed { index, group ->
                OptionCard(
                    group = group,
                    isExpanded = uiState.optionGroupIndex == index || !uiState.orderBegin,
                    selectedNames = uiState.selectedNamesOf(group),
                    showSelectionError = uiState.showSelectionError &&
                        uiState.optionGroupIndex == index,
                    isSelected = { optionId -> uiState.isSelected(group.id, optionId) },
                    onOptionClick = { optionId -> onOptionClick(group.id, optionId) },
                    onCardClick = { onCardClick(index) },
                )
            }
            if (uiState.isPersonalCardVisible) {
                PersonalOptionCard(
                    appliedOptions = uiState.appliedPersonalOptions(),
                    onClick = onPersonalClick,
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    group: OptionGroup,
    isExpanded: Boolean,
    selectedNames: String,
    showSelectionError: Boolean,
    isSelected: (String) -> Boolean,
    onOptionClick: (String) -> Unit,
    onCardClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = CardShape, clip = false)
            .clip(CardShape)
            .background(MammothTheme.colors.surface.background.bg1)
            .clickableNoRipple(enabled = !isExpanded, onClick = onCardClick)
            .defaultMinSize(minHeight = CollapsedCardHeight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MammothText(
                text = group.name,
                style = if (isExpanded) {
                    MammothTheme.typography.title
                } else {
                    MammothTheme.typography.body
                },
                fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                color = if (isExpanded) {
                    MammothTheme.colors.text.primary
                } else {
                    MammothTheme.colors.text.unselected
                },
                modifier = Modifier.weight(1f, fill = false),
            )
            // 접힌 카드에는 고른 값을 오른쪽에 보여준다.
            if (!isExpanded && selectedNames.isNotBlank()) {
                MammothText(
                    text = selectedNames,
                    style = MammothTheme.typography.title,
                    color = MammothTheme.colors.text.secondary,
                    ellipsis = true,
                )
            }
        }

        if (isExpanded) {
            if (showSelectionError || group.minSelection > 1) {
                MammothText(
                    text = "최소 ${group.minSelection.coerceAtLeast(1)}개 선택",
                    style = MammothTheme.typography.label,
                    color = if (showSelectionError) {
                        MammothTheme.colors.text.error
                    } else {
                        MammothTheme.colors.text.unselected
                    },
                )
            }
            OptionGrid(
                group = group,
                isSelected = isSelected,
                onOptionClick = onOptionClick,
            )
        }
    }
}

@Composable
private fun OptionGrid(
    group: OptionGroup,
    isSelected: (String) -> Boolean,
    onOptionClick: (String) -> Unit,
) {
    // 웹과 같이 옵션이 3개 이상이면 3열, 아니면 2열
    val columns = if (group.options.size >= 3) 3 else 2
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        group.options.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                row.forEach { option ->
                    OptionButton(
                        name = option.name,
                        extraPrice = option.salePrice,
                        selected = isSelected(option.id),
                        enabled = option.isChangeable,
                        onClick = { onOptionClick(option.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun OptionButton(
    name: String,
    extraPrice: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MammothTheme.colors
    val contentColor = when {
        !enabled -> colors.text.disabled
        selected -> colors.text.accent
        else -> colors.text.primary
    }
    Column(
        modifier = modifier
            .clip(MammothShapes.sm)
            .background(if (selected) colors.primaryContainer else colors.surface.background.bg1)
            .border(
                MammothBorderWidth,
                if (selected) colors.surface.border.accent else colors.surface.border.default,
                MammothShapes.sm,
            )
            .clickableNoRipple(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        MammothText(
            text = name,
            style = MammothTheme.typography.label,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        if (extraPrice != 0) {
            MammothText(
                text = if (extraPrice > 0) "+${extraPrice.toPriceText()}" else extraPrice.toPriceText(),
                style = MammothTheme.typography.caption,
                color = colors.text.secondary,
            )
        }
    }
}

/** 웹 PersonalOptionCard: 기본 옵션을 다 고르면 나타나고, 누르면 퍼스널 옵션 시트가 열린다. */
@Composable
private fun PersonalOptionCard(
    appliedOptions: List<Triple<String, Int, Int>>,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = CardShape, clip = false)
            .clip(CardShape)
            .background(MammothTheme.colors.surface.background.bg1)
            .clickableNoRipple(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MammothText(
                text = "퍼스널 옵션",
                style = MammothTheme.typography.headline,
                fontWeight = FontWeight.Bold,
            )
            MammothButton(
                text = if (appliedOptions.isEmpty()) "옵션추가" else "옵션수정",
                onClick = onClick,
                size = MammothButtonSize.Xs,
                variant = MammothButtonVariant.Line,
            )
        }
        appliedOptions.forEach { (name, quantity, amount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MammothText(
                    text = "$name x $quantity",
                    style = MammothTheme.typography.label,
                    color = MammothTheme.colors.text.secondary,
                )
                MammothText(
                    text = amount.toPriceText(),
                    color = MammothTheme.colors.text.secondary,
                )
            }
        }
    }
}
