package com.brahim.meteo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

/**
 * Expressive type scale: heavier display/title styles and the new emphasized variants.
 */
val AppTypography =
    Typography(
        displayLarge =
            base.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp,
            ),
        displayMedium =
            base.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
            ),
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        displayLargeEmphasized =
            base.displayLargeEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp,
            ),
        displayMediumEmphasized =
            base.displayMediumEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
            ),
        displaySmallEmphasized = base.displaySmallEmphasized.copy(fontWeight = FontWeight.Bold),
        headlineLargeEmphasized = base.headlineLargeEmphasized.copy(fontWeight = FontWeight.Bold),
        headlineMediumEmphasized =
            base.headlineMediumEmphasized.copy(fontWeight = FontWeight.SemiBold),
        headlineSmallEmphasized =
            base.headlineSmallEmphasized.copy(fontWeight = FontWeight.SemiBold),
        titleLargeEmphasized = base.titleLargeEmphasized.copy(fontWeight = FontWeight.Bold),
        titleMediumEmphasized = base.titleMediumEmphasized.copy(fontWeight = FontWeight.SemiBold),
        titleSmallEmphasized = base.titleSmallEmphasized.copy(fontWeight = FontWeight.SemiBold),
        labelLargeEmphasized =
            base.labelLargeEmphasized.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp,
            ),
        labelMediumEmphasized =
            base.labelMediumEmphasized.copy(fontWeight = FontWeight.SemiBold),
    )
