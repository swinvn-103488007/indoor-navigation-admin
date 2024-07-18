package com.example.indoor_navigation.domain.ml

import com.example.indoor_navigation.data.model.DetectedObjectResult
import com.google.ar.core.Frame

data class DetectedText(
    val detectedObjectResult: DetectedObjectResult,
    val frame: Frame
)
