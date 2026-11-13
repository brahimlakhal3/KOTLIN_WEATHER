package com.brahim.meteo.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

enum class CornerStyle {
  EXPRESSIVE,
  ROUND,
  COMPACT,
  SQUIRCLE,
  PILLED;

  val shapes: Shapes
    get() =
        when (this) {
          EXPRESSIVE ->
              Shapes(
                  extraSmall = RoundedCornerShape(12.dp),
                  small = RoundedCornerShape(18.dp),
                  medium = RoundedCornerShape(26.dp),
                  large = RoundedCornerShape(34.dp),
                  extraLarge = RoundedCornerShape(44.dp),
              )
          ROUND ->
              Shapes(
                  extraSmall = RoundedCornerShape(8.dp),
                  small = RoundedCornerShape(14.dp),
                  medium = RoundedCornerShape(20.dp),
                  large = RoundedCornerShape(28.dp),
                  extraLarge = RoundedCornerShape(36.dp),
              )
          COMPACT ->
              Shapes(
                  extraSmall = RoundedCornerShape(4.dp),
                  small = RoundedCornerShape(8.dp),
                  medium = RoundedCornerShape(12.dp),
                  large = RoundedCornerShape(18.dp),
                  extraLarge = RoundedCornerShape(24.dp),
              )
          SQUIRCLE ->
              Shapes(
                  extraSmall = SquircleShape(CornerSize(8.dp), squareness = 6f),
                  small = SquircleShape(CornerSize(14.dp), squareness = 6f),
                  medium = SquircleShape(CornerSize(22.dp), squareness = 5.5f),
                  large = SquircleShape(CornerSize(30.dp), squareness = 5f),
                  extraLarge = SquircleShape(CornerSize(40.dp), squareness = 5f),
              )
          PILLED ->
              Shapes(
                  extraSmall = RoundedCornerShape(percent = 50),
                  small = RoundedCornerShape(percent = 50),
                  medium = RoundedCornerShape(percent = 50),
                  large = RoundedCornerShape(percent = 50),
                  extraLarge = RoundedCornerShape(percent = 50),
              )
        }

  companion object {
    fun from(value: String?): CornerStyle =
        entries.firstOrNull { entry -> entry.name == value } ?: EXPRESSIVE
  }
}

val AppShapes: Shapes = CornerStyle.EXPRESSIVE.shapes
