package com.example.mammothnative.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothSpacing
import com.example.mammothnative.ui.theme.MammothTheme

/** 아직 네이티브로 옮기지 않은 탭을 위한 자리. */
@Composable
fun PlaceholderScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MammothTheme.colors.surface.background.bg1)
            .padding(MammothSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(MammothSpacing.sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MammothText(
            text = title,
            style = MammothTheme.typography.headline,
            fontWeight = FontWeight.Bold,
        )
        MammothText(
            text = description,
            color = MammothTheme.colors.text.secondary,
            textAlign = TextAlign.Center,
        )
    }
}
