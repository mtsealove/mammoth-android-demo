package com.example.mammothnative.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothSpacing
import com.example.mammothnative.ui.theme.MammothTheme

enum class MammothButtonSize { Xs, Sm, Md, Lg }

enum class MammothButtonVariant {
    /** 브랜드 컬러 채움 — 주요 액션 */
    Filled,

    /** 베이지 톤 채움 — 보조 액션 */
    Secondary,

    /** 테두리만 있는 버튼 */
    Line,

    /** 배경 없는 텍스트 버튼 */
    Text,

    /** 클릭은 되지만 비활성처럼 보여야 할 때 (웹 `variant-disabled`) */
    Disabled,
}

enum class MammothButtonShape { Round, Square }

enum class MammothButtonState { Default, Unselected }

/**
 * 웹 `components/Button` 대응 컴포넌트.
 *
 * ```
 * MammothButton(
 *     text = "장바구니 담기",
 *     onClick = ::addToCart,
 *     size = MammothButtonSize.Lg,
 *     fullWidth = true,
 *     isCta = true,
 * )
 * ```
 */
@Composable
fun MammothButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: MammothButtonSize = MammothButtonSize.Md,
    variant: MammothButtonVariant = MammothButtonVariant.Filled,
    shape: MammothButtonShape = MammothButtonShape.Round,
    state: MammothButtonState = MammothButtonState.Default,
    fullWidth: Boolean = false,
    /** 화면 하단 고정 버튼. 제스처 내비게이션 영역만큼 아래 패딩을 더 준다. */
    isCta: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val spec = size.spec()
    MammothButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        variant = variant,
        shape = shape,
        state = state,
        fullWidth = fullWidth,
        isCta = isCta,
    ) {
        leadingIcon?.let { ButtonIcon(size = spec.iconSize, content = it) }
        MammothText(
            text = text,
            style = MammothTheme.typography.body.copy(fontSize = spec.fontSize),
            fontWeight = spec.fontWeight(variant),
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
        )
        trailingIcon?.let { ButtonIcon(size = spec.iconSize, content = it) }
    }
}

/**
 * 내용을 직접 구성해야 할 때 쓰는 슬롯 버전.
 * 가격·수량처럼 두 덩어리를 양쪽으로 벌려야 하는 CTA 등에 사용한다.
 */
@Composable
fun MammothButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: MammothButtonSize = MammothButtonSize.Md,
    variant: MammothButtonVariant = MammothButtonVariant.Filled,
    shape: MammothButtonShape = MammothButtonShape.Round,
    state: MammothButtonState = MammothButtonState.Default,
    fullWidth: Boolean = false,
    isCta: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        MammothSpacing.xs,
        Alignment.CenterHorizontally,
    ),
    content: @Composable RowScope.() -> Unit,
) {
    val spec = size.spec()
    val palette = buttonColors(variant = variant, state = state, enabled = enabled)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // 웹 `pressActionEffect` 와 동일한 눌림 축소 효과
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonPressScale",
    )
    val containerColor by animateColorAsState(
        targetValue = palette.container,
        animationSpec = tween(durationMillis = 150),
        label = "buttonContainerColor",
    )

    val buttonShape = spec.shape(shape)
    val bottomPadding = if (isCta) {
        val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
        maxOf(navigationBarPadding, spec.verticalPadding)
    } else {
        spec.verticalPadding
    }

    Row(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .scale(pressScale)
            .clip(buttonShape)
            .background(containerColor)
            .then(
                if (palette.border != null) {
                    Modifier.border(MammothBorderWidth, palette.border, buttonShape)
                } else {
                    Modifier
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                // 웹은 ripple 없이 눌림 축소만 쓰므로 indication 을 끈다.
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                start = spec.horizontalPadding,
                end = spec.horizontalPadding,
                top = spec.verticalPadding,
                bottom = bottomPadding,
            ),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides palette.content) {
            content()
        }
    }
}

@Composable
private fun ButtonIcon(size: Dp, content: @Composable () -> Unit) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) { content() }
}

@Immutable
private data class ButtonPalette(
    val container: Color,
    val content: Color,
    val border: Color?,
)

@Composable
private fun buttonColors(
    variant: MammothButtonVariant,
    state: MammothButtonState,
    enabled: Boolean,
): ButtonPalette {
    val colors = MammothTheme.colors
    val unselected = state == MammothButtonState.Unselected
    return when (variant) {
        MammothButtonVariant.Filled -> if (enabled) {
            ButtonPalette(colors.surface.button.default, colors.text.reverse, null)
        } else {
            ButtonPalette(colors.surface.button.disabled, colors.text.disabled, null)
        }

        MammothButtonVariant.Disabled ->
            ButtonPalette(colors.surface.button.disabled, colors.text.disabled, null)

        MammothButtonVariant.Secondary -> if (enabled) {
            ButtonPalette(colors.surface.button.secondary, colors.text.accent, null)
        } else {
            ButtonPalette(colors.surface.button.unselected, colors.text.unselected, null)
        }

        MammothButtonVariant.Line -> when {
            !enabled -> ButtonPalette(
                Color.Transparent,
                colors.text.disabled,
                colors.icon.disabled,
            )

            unselected -> ButtonPalette(
                Color.Transparent,
                colors.text.unselected,
                colors.surface.border.default,
            )

            else -> ButtonPalette(
                Color.Transparent,
                colors.text.primary,
                colors.surface.border.default,
            )
        }

        MammothButtonVariant.Text -> ButtonPalette(
            Color.Transparent,
            if (enabled) colors.text.primary else colors.text.disabled,
            null,
        )
    }
}

@Immutable
private data class ButtonSizeSpec(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val fontSize: TextUnit,
    val baseFontWeight: FontWeight,
    val iconSize: Dp,
    val roundShape: Shape,
) {
    fun shape(shape: MammothButtonShape): Shape = when (shape) {
        MammothButtonShape.Round -> roundShape
        MammothButtonShape.Square -> MammothShapes.none
    }

    /** 웹에서 text 변형은 크기와 무관하게 medium 이다. */
    fun fontWeight(variant: MammothButtonVariant): FontWeight =
        if (variant == MammothButtonVariant.Text) FontWeight.Medium else baseFontWeight
}

private fun MammothButtonSize.spec(): ButtonSizeSpec = when (this) {
    MammothButtonSize.Xs -> ButtonSizeSpec(
        horizontalPadding = 12.dp,
        verticalPadding = 8.dp,
        fontSize = 12.sp,
        baseFontWeight = FontWeight.Medium,
        iconSize = 16.dp,
        roundShape = MammothShapes.sm,
    )

    MammothButtonSize.Sm -> ButtonSizeSpec(
        horizontalPadding = 16.dp,
        verticalPadding = 8.dp,
        fontSize = 14.sp,
        baseFontWeight = FontWeight.Bold,
        iconSize = 20.dp,
        roundShape = MammothShapes.sm,
    )

    MammothButtonSize.Md -> ButtonSizeSpec(
        horizontalPadding = 24.dp,
        verticalPadding = 12.dp,
        fontSize = 16.sp,
        baseFontWeight = FontWeight.Bold,
        iconSize = 24.dp,
        roundShape = MammothShapes.md,
    )

    MammothButtonSize.Lg -> ButtonSizeSpec(
        horizontalPadding = 24.dp,
        verticalPadding = 16.dp,
        fontSize = 16.sp,
        baseFontWeight = FontWeight.Bold,
        iconSize = 24.dp,
        roundShape = MammothShapes.pill,
    )
}
