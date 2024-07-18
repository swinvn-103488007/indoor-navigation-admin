package com.example.indoor_navigation.data.model

import com.google.ar.sceneform.math.Vector3
import dev.romainguy.kotlin.math.Float2

data class DetectedObjectResult(
    val label: String,
    val centerCoordinate: Float2,
)