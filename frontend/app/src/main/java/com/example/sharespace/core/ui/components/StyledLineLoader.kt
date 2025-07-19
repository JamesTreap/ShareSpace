package com.example.sharespace.core.ui.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.BorderPrimary

@Composable
fun StyledLineLoader(
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        modifier = modifier.height(4.dp),
        color = AquaAccent,
        trackColor = BorderPrimary
    )
}