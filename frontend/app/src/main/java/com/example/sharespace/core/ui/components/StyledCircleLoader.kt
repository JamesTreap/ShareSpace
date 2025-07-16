package com.example.sharespace.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.BorderPrimary

@Composable
fun StyledCircleLoader(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.size(40.dp),
        color = AquaAccent,           // Active spinning arc
        trackColor = BorderPrimary,   // Background track color
        strokeWidth = 4.dp
    )
}