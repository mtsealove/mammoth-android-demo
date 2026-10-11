package com.example.mammothnative.ui.component

import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.example.mammothnative.ui.theme.MammothTheme

/**
 * 앱에서 쓰는 바텀시트.
 *
 * Material3 의 ModalBottomSheet 은 자체 Dialog 창에 뜨는데, 기본 창은 시스템 바 영역을
 * 침범하지 않아 상태바 쪽만 어두워지지 않는다. 창에 LAYOUT_NO_LIMITS 를 걸어
 * 스크림이 safe area(상태바·내비게이션바)까지 덮도록 한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MammothModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // SheetState 가 실험적 API 라 호출부로 새어나가지 않게 여기서만 만든다.
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MammothTheme.colors.surface.background.bg1,
    ) {
        ExtendScrimToSystemBars()
        content()
    }
}

@Composable
private fun ExtendScrimToSystemBars() {
    val view = LocalView.current
    SideEffect {
        // M3 의 ModalBottomSheet 은 Dialog 가 아니라 WindowManager 에 직접 붙는
        // ModalBottomSheetWindow 로 뜬다. 그 창에 LAYOUT_NO_LIMITS 를 걸어야
        // 스크림이 상태바·내비게이션바 뒤까지 덮는다.
        val sheetWindow = generateSequence(view.parent) { (it as? View)?.parent }
            .filterIsInstance<View>()
            .firstOrNull { it.layoutParams is WindowManager.LayoutParams }
            ?: return@SideEffect
        val params = sheetWindow.layoutParams as WindowManager.LayoutParams
        if (params.flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS != 0) {
            return@SideEffect
        }
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        val windowManager = sheetWindow.context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.updateViewLayout(sheetWindow, params)
    }
}
