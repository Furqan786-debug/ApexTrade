package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.ApexTradeApp
import com.example.ui.theme.ApexTradeTheme
import com.example.ui.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApexTradeTheme {
                ApexTradeApp(viewModel = viewModel)
            }
        }
    }
}
