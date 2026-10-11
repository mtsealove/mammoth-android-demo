package com.example.mammothnative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mammothnative.ui.MammothApp
import com.example.mammothnative.ui.theme.MammothTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱의 유일한 액티비티. 웹의 MainActivity(하단 탭 + 프래그먼트)와 로그인 화면을 [MammothApp] 이 담당한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MammothTheme {
                MammothApp()
            }
        }
    }
}
