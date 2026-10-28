package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralEnd
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart

/**
 * Standard Gradient Brush for primary buttons across Coconut PFA.
 */
val PrimaryOrangeGradient = Brush.horizontalGradient(
    colors = listOf(GradientOrangeCoralStart, GradientOrangeCoralEnd)
)

/**
 * Custom primary button with vibrant orange linear gradient background.
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: ButtonElevation? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val gradientBrush = if (enabled) {
        PrimaryOrangeGradient
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        elevation = elevation,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        contentPadding = contentPadding,
        modifier = modifier
            .clip(shape)
            .background(brush = gradientBrush)
    ) {
        content()
    }
}

/**
 * Floating Action Button with vibrant orange linear gradient background.
 */
@Composable
fun GradientFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    contentColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = shape,
        containerColor = Color.Transparent,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .background(brush = PrimaryOrangeGradient)
    ) {
        content()
    }
}

/**
 * Extended Floating Action Button with vibrant orange linear gradient background.
 */
@Composable
fun GradientExtendedFloatingActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentColor: Color = Color.White,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = icon,
        text = text,
        shape = shape,
        containerColor = Color.Transparent,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .background(brush = PrimaryOrangeGradient)
    )
}
