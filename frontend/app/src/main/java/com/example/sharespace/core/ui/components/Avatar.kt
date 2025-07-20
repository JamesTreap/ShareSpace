package com.example.sharespace.core.ui.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.example.sharespace.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun Avatar(
    photoUrl: String?, contentDescription: String? = null, size: Dp = 56.dp
) {
    val localProfileMap = mapOf(
        "pfp0" to R.drawable.pfp0,
        "pfp1" to R.drawable.pfp1,
        "pfp2" to R.drawable.pfp2,
        "pfp3" to R.drawable.pfp3,
        "pfp4" to R.drawable.pfp4,
        "pfp5" to R.drawable.pfp5,
        "pfp6" to R.drawable.pfp6,
        "pfp7" to R.drawable.pfp7,
        "pfp8" to R.drawable.pfp8,
        "pfp9" to R.drawable.pfp9
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant) // subtle backdrop
    ) {
        when {
            photoUrl != null && localProfileMap.containsKey(photoUrl) -> {
                Image(
                    painter = painterResource(id = localProfileMap[photoUrl]!!),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(CircleShape)
                )
            }

            photoUrl != null -> {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(CircleShape)
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(12.dp)
                )
            }
        }
    }
}