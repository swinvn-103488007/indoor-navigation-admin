package com.example.indoor_navigation.domain.hit_test

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion

data class OrientatedPosition(
    val position: Float3,
    val orientation: Quaternion
)