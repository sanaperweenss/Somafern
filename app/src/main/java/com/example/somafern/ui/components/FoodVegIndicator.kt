package com.example.somafern.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.somafern.data.model.FoodItem
import com.example.somafern.data.model.isVegItem
import com.example.somafern.ui.theme.AccentGreen
import com.example.somafern.ui.theme.DangerRed
import com.example.somafern.ui.theme.NavyBackground

@Composable
fun FoodVegIndicator(
    dish: FoodItem,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    dotSize: Dp = 7.dp
) {
    val isVeg = dish.isVegItem
    val color = if (isVeg) AccentGreen else DangerRed

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(NavyBackground.copy(alpha = 0.85f))
            .border(1.2.dp, color, RoundedCornerShape(4.dp))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(color)
        )
    }
}
