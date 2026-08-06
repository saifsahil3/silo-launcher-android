package com.example.ui.labs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LabsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Labs screen is compiled out / unavailable in Production builds
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Labs mode is unavailable in production builds.", color = Color.Gray)
    }
}
