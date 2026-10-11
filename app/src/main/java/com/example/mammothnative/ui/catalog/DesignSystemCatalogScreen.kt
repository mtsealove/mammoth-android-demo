package com.example.mammothnative.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonShape
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonState
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothPalette
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothSpacing
import com.example.mammothnative.ui.theme.MammothTheme

/**
 * 디자인 토큰과 공통 컴포넌트를 눈으로 확인하는 화면.
 * 실제 기능 화면을 붙이기 전까지 임시 홈으로 쓴다.
 */
@Composable
fun DesignSystemCatalogScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MammothTheme.colors.surface.background.bg1),
        contentPadding = PaddingValues(
            start = MammothSpacing.screenHorizontal,
            end = MammothSpacing.screenHorizontal,
            top = MammothSpacing.xl,
            bottom = MammothSpacing.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(MammothSpacing.xl),
    ) {
        item {
            MammothText(
                text = "Mammoth Design System",
                style = MammothTheme.typography.headline2,
                fontWeight = FontWeight.Bold,
            )
        }

        item { ColorSection() }
        item { TypographySection() }
        item { ButtonSizeSection() }
        item { ButtonVariantSection() }
        item { ButtonStateSection() }
        item { ButtonLayoutSection() }
    }
}

@Composable
private fun SectionTitle(text: String) {
    MammothText(
        text = text,
        style = MammothTheme.typography.headline,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun ColorSection() {
    Column(verticalArrangement = Arrangement.spacedBy(MammothSpacing.md)) {
        SectionTitle("Colors")
        ColorRow(
            "Gray",
            listOf(
                MammothPalette.Gray0, MammothPalette.Gray1, MammothPalette.Gray2,
                MammothPalette.Gray3, MammothPalette.Gray4, MammothPalette.Gray5,
                MammothPalette.Gray6, MammothPalette.Gray7, MammothPalette.Gray8,
                MammothPalette.Gray9,
            ),
        )
        ColorRow(
            "Primary / Secondary",
            listOf(
                MammothPalette.Primary1, MammothPalette.Primary2, MammothPalette.Primary3,
                MammothPalette.Secondary1, MammothPalette.Secondary2,
            ),
        )
        ColorRow(
            "System",
            listOf(
                MammothPalette.Red, MammothPalette.RedOpacity8,
                MammothPalette.Green, MammothPalette.GreenOpacity8,
                MammothPalette.Blue, MammothPalette.BlueOpacity8,
            ),
        )
    }
}

@Composable
private fun ColorRow(label: String, colors: List<Color>) {
    Column(verticalArrangement = Arrangement.spacedBy(MammothSpacing.sm)) {
        MammothText(
            text = label,
            style = MammothTheme.typography.label,
            color = MammothTheme.colors.text.secondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MammothSpacing.sm)) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MammothShapes.sm)
                        .background(color)
                        .border(
                            MammothBorderWidth,
                            MammothTheme.colors.surface.border.default,
                            MammothShapes.sm,
                        ),
                )
            }
        }
    }
}

@Composable
private fun TypographySection() {
    Column(verticalArrangement = Arrangement.spacedBy(MammothSpacing.sm)) {
        SectionTitle("Typography")
        MammothText("Headline2 20 · Bold", style = MammothTheme.typography.headline2, fontWeight = FontWeight.Bold)
        MammothText("Headline 18 · Bold", style = MammothTheme.typography.headline, fontWeight = FontWeight.Bold)
        MammothText("Title 16 · SemiBold", style = MammothTheme.typography.title, fontWeight = FontWeight.SemiBold)
        MammothText("Body 14 · Medium 매머드커피", style = MammothTheme.typography.body, fontWeight = FontWeight.Medium)
        MammothText("Label 12 · Regular 매머드커피", style = MammothTheme.typography.label)
        MammothText("Caption 10 · Regular 매머드커피", style = MammothTheme.typography.caption)
    }
}

@Composable
private fun ButtonSizeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(MammothSpacing.md)) {
        SectionTitle("Button · size")
        Row(
            horizontalArrangement = Arrangement.spacedBy(MammothSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MammothButton(text = "xs", onClick = {}, size = MammothButtonSize.Xs)
            MammothButton(text = "sm", onClick = {}, size = MammothButtonSize.Sm)
            MammothButton(text = "md", onClick = {}, size = MammothButtonSize.Md)
        }
        MammothButton(
            text = "lg · fullWidth",
            onClick = {},
            size = MammothButtonSize.Lg,
            fullWidth = true,
        )
        MammothButton(
            text = "md · square",
            onClick = {},
            shape = MammothButtonShape.Square,
            fullWidth = true,
        )
    }
}

@Composable
private fun ButtonVariantSection() {
    Column(verticalArrangement = Arrangement.spacedBy(MammothSpacing.sm)) {
        SectionTitle("Button · variant")
        MammothButton(text = "filled", onClick = {}, fullWidth = true)
        MammothButton(
            text = "secondary",
            onClick = {},
            variant = MammothButtonVariant.Secondary,
            fullWidth = true,
        )
        MammothButton(
            text = "line",
            onClick = {},
            variant = MammothButtonVariant.Line,
            fullWidth = true,
        )
        MammothButton(
            text = "text",
            onClick = {},
            variant = MammothButtonVariant.Text,
            fullWidth = true,
        )
        MammothButton(
            text = "disabled 처럼 보이지만 눌림",
            onClick = {},
            variant = MammothButtonVariant.Disabled,
            fullWidth = true,
        )
    }
}

@Composable
private fun ButtonStateSection() {
    Column(verticalArrangement = Arrangement.spacedBy(MammothSpacing.sm)) {
        SectionTitle("Button · enabled / unselected")
        MammothButton(text = "filled disabled", onClick = {}, enabled = false, fullWidth = true)
        MammothButton(
            text = "secondary disabled",
            onClick = {},
            variant = MammothButtonVariant.Secondary,
            enabled = false,
            fullWidth = true,
        )
        MammothButton(
            text = "line unselected",
            onClick = {},
            variant = MammothButtonVariant.Line,
            state = MammothButtonState.Unselected,
            fullWidth = true,
        )
    }
}

@Composable
private fun ButtonLayoutSection() {
    Column(verticalArrangement = Arrangement.spacedBy(MammothSpacing.sm)) {
        SectionTitle("Button · 슬롯 사용")
        MammothButton(
            onClick = {},
            size = MammothButtonSize.Lg,
            fullWidth = true,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MammothText(
                text = "장바구니 담기",
                style = MammothTheme.typography.title,
                fontWeight = FontWeight.Bold,
                color = MammothTheme.colors.text.reverse,
            )
            MammothText(
                text = "12,800원",
                style = MammothTheme.typography.title,
                fontWeight = FontWeight.Bold,
                color = MammothTheme.colors.text.reverse,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun DesignSystemCatalogPreview() {
    MammothTheme {
        DesignSystemCatalogScreen()
    }
}
