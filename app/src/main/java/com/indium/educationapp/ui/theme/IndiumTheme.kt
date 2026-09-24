package com.indium.educationapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// -----------------------------------------
// INDIUM LAVENDER COLOUR PALETTE
// -----------------------------------------

val IndiumLavender = Color(0xFF7C4DFF)
val IndiumDeepViolet = Color(0xFF673AB7)
val IndiumLightBackground = Color(0xFFF7F4FF)
val IndiumWhite = Color(0xFFFFFFFF)
val IndiumDarkText = Color(0xFF252238)
val IndiumGreyText = Color(0xFF686477)
val IndiumLightBorder = Color(0xFFE6DDFB)

private val IndiumColorScheme = lightColorScheme(
    primary = IndiumDeepViolet,
    onPrimary = IndiumWhite,

    secondary = IndiumLavender,
    onSecondary = IndiumWhite,

    background = IndiumLightBackground,
    onBackground = IndiumDarkText,

    surface = IndiumWhite,
    onSurface = IndiumDarkText,

    surfaceVariant = Color(0xFFEDE7F6),
    onSurfaceVariant = IndiumGreyText,

    outline = IndiumLightBorder
)

// -----------------------------------------
// INDIUM APP THEME
// -----------------------------------------

@Composable
fun IndiumAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndiumColorScheme,
        content = content
    )
}
