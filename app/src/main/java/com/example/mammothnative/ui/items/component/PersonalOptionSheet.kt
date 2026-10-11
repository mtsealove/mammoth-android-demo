package com.example.mammothnative.ui.items.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mammothnative.R
import com.example.mammothnative.domain.model.OptionGroup
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonShape
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothModalBottomSheet
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.items.ItemUiState
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple
import com.example.mammothnative.ui.util.toPriceText

/**
 * 웹 PersonalOptionBottomSheet.
 * 왼쪽에 옵션 그룹, 오른쪽에 그 그룹의 옵션이 뜨고, '옵션 적용'을 눌러야 반영된다.
 */
@Composable
fun PersonalOptionSheet(
    uiState: ItemUiState,
    onDismiss: () -> Unit,
    onGroupSelect: (Int) -> Unit,
    onOptionClick: (groupId: String, optionId: String) -> Unit,
    onQuantityChange: (groupId: String, optionId: String, delta: Int) -> Unit,
    onApply: () -> Unit,
) {
    val groups = uiState.personalGroups
    val selectedGroup = groups.getOrNull(uiState.personalGroupIndex) ?: return

    MammothModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Row(modifier = Modifier.heightIn(min = 240.dp, max = 420.dp)) {
                GroupColumn(
                    groups = groups,
                    selectedIndex = uiState.personalGroupIndex,
                    onSelect = onGroupSelect,
                )
                OptionColumn(
                    group = selectedGroup,
                    uiState = uiState,
                    onOptionClick = onOptionClick,
                    onQuantityChange = onQuantityChange,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MammothText(
                    text = "추가 금액",
                    color = MammothTheme.colors.text.secondary,
                )
                MammothText(
                    text = uiState.personalDraftPrice.toPriceText(),
                    style = MammothTheme.typography.headline2,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                MammothButton(
                    text = "닫기",
                    onClick = onDismiss,
                    size = MammothButtonSize.Lg,
                    shape = MammothButtonShape.Square,
                    variant = MammothButtonVariant.Secondary,
                    fullWidth = true,
                    modifier = Modifier.weight(1f),
                )
                MammothButton(
                    text = "옵션 적용",
                    onClick = onApply,
                    size = MammothButtonSize.Lg,
                    shape = MammothButtonShape.Square,
                    fullWidth = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun GroupColumn(
    groups: List<OptionGroup>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .width(110.dp)
            .background(MammothTheme.colors.surface.background.bg3),
    ) {
        itemsIndexed(groups, key = { _, group -> group.id }) { index, group ->
            val selected = index == selectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableNoRipple { onSelect(index) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MammothText(
                    text = group.name,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) {
                        MammothTheme.colors.text.accent
                    } else {
                        MammothTheme.colors.text.unselected
                    },
                )
            }
        }
    }
}

@Composable
private fun OptionColumn(
    group: OptionGroup,
    uiState: ItemUiState,
    onOptionClick: (groupId: String, optionId: String) -> Unit,
    onQuantityChange: (groupId: String, optionId: String, delta: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        group.options.forEach { option ->
            val quantity = uiState.draftQuantityOf(group.id, option.id)
            val selected = quantity > 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionIndicator(
                    selected = selected,
                    isSingleSelect = group.isSingleSelect,
                    onClick = { onOptionClick(group.id, option.id) },
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickableNoRipple { onOptionClick(group.id, option.id) },
                ) {
                    MammothText(text = option.name, fontWeight = FontWeight.Medium)
                    if (option.salePrice != 0) {
                        MammothText(
                            text = if (option.salePrice > 0) {
                                "+${option.salePrice.toPriceText()}"
                            } else {
                                option.salePrice.toPriceText()
                            },
                            style = MammothTheme.typography.label,
                            color = MammothTheme.colors.text.secondary,
                        )
                    }
                }
                // 수량을 늘릴 수 있는 옵션(샷 추가 등)만 스테퍼를 보여준다.
                if (selected && option.maxQuantity > 1) {
                    QuantityStepper(
                        quantity = quantity,
                        onChange = { delta -> onQuantityChange(group.id, option.id, delta) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    isSingleSelect: Boolean,
    onClick: () -> Unit,
) {
    val shape = if (isSingleSelect) CircleShape else MammothShapes.sm
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(shape)
            .background(
                if (selected) MammothTheme.colors.primary else MammothTheme.colors.surface.background.bg1,
            )
            .border(
                MammothBorderWidth,
                if (selected) MammothTheme.colors.primary else MammothTheme.colors.surface.border.default,
                shape,
            )
            .clickableNoRipple(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MammothTheme.colors.text.reverse),
            )
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onChange: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(iconRes = R.drawable.ic_minus, description = "수량 줄이기") { onChange(-1) }
        MammothText(text = "$quantity", fontWeight = FontWeight.Bold)
        StepperButton(iconRes = R.drawable.ic_plus, description = "수량 늘리기") { onChange(1) }
    }
}

@Composable
private fun StepperButton(iconRes: Int, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(MammothBorderWidth, MammothTheme.colors.surface.border.default, CircleShape)
            .clickableNoRipple(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = MammothTheme.colors.icon.default,
            modifier = Modifier.size(12.dp),
        )
    }
}
