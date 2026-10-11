package com.example.mammothnative.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.example.mammothnative.ui.theme.MammothTheme
import androidx.compose.material3.Text as MaterialText

/**
 * 웹 `components/Text` 대응 컴포넌트.
 *
 * 크기는 [MammothTheme.typography] 토큰(`body`, `title`, `headline` ...)으로,
 * 굵기는 [fontWeight] 로 따로 지정한다. 웹과 동일하게 크기·굵기가 분리된 구조다.
 *
 * ```
 * MammothText("주문하기", style = MammothTheme.typography.title, fontWeight = FontWeight.Bold)
 * ```
 */
@Composable
fun MammothText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MammothTheme.typography.body,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    /** true 면 한 줄로 자르고 말줄임표를 붙인다 (웹 `ellipsis`) */
    ellipsis: Boolean = false,
    underline: Boolean = false,
    enabled: Boolean = true,
) {
    MaterialText(
        text = text,
        modifier = modifier,
        color = resolveColor(color, enabled),
        fontWeight = fontWeight,
        textAlign = textAlign,
        textDecoration = if (underline) TextDecoration.Underline else null,
        overflow = if (ellipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
        maxLines = if (ellipsis) 1 else maxLines,
        style = style,
    )
}

@Composable
fun MammothText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MammothTheme.typography.body,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    ellipsis: Boolean = false,
    underline: Boolean = false,
    enabled: Boolean = true,
) {
    MaterialText(
        text = text,
        modifier = modifier,
        color = resolveColor(color, enabled),
        fontWeight = fontWeight,
        textAlign = textAlign,
        textDecoration = if (underline) TextDecoration.Underline else null,
        overflow = if (ellipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
        maxLines = if (ellipsis) 1 else maxLines,
        style = style,
    )
}

@Composable
private fun resolveColor(color: Color, enabled: Boolean): Color = when {
    !enabled -> MammothTheme.colors.text.disabled
    color != Color.Unspecified -> color
    else -> MammothTheme.colors.text.primary
}
