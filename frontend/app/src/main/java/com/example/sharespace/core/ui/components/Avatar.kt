package com.example.sharespace.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sharespace.R

@Composable
fun Avatar(
    photoUrl: String?,
    contentDescription: String? = null,
    size: Dp = 56.dp,
    shape: Shape = CircleShape,              // <-- new
) {
    val localProfileMap = mapOf(
        "pfp0" to R.drawable.pfp0, "pfp1" to R.drawable.pfp1, "pfp2" to R.drawable.pfp2,
        "pfp3" to R.drawable.pfp3, "pfp4" to R.drawable.pfp4, "pfp5" to R.drawable.pfp5,
        "pfp6" to R.drawable.pfp6, "pfp7" to R.drawable.pfp7, "pfp8" to R.drawable.pfp8,
        "pfp9" to R.drawable.pfp9
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when {
            photoUrl != null && localProfileMap.containsKey(photoUrl) -> {
                Image(
                    painter = painterResource(localProfileMap[photoUrl]!!),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(shape)
                )
            }
            photoUrl != null -> {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(shape)
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

/** Convenience helper for the task list look (rounded square). */
@Composable
fun AvatarSquare(
    photoUrl: String?,
    contentDescription: String? = null,
    size: Dp = 56.dp,
    cornerRadius: Dp = 8.dp,
) = Avatar(
    photoUrl = photoUrl,
    contentDescription = contentDescription,
    size = size,
    shape = RoundedCornerShape(cornerRadius)
)
