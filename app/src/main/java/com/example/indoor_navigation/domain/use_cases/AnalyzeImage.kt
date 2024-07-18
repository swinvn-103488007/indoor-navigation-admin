package com.example.indoor_navigation.domain.use_cases

import android.media.Image
import com.example.indoor_navigation.data.model.DetectedObjectResult
import com.example.indoor_navigation.domain.ml.ObjectDetector

class AnalyzeImage(
    private val objectDetector: ObjectDetector,
) {

    suspend operator fun invoke(
        image: Image,
        imageRotation: Int,
        imageCropPercentage: Pair<Int, Int>,
        displaySize: Pair<Int, Int>
    ): Result<DetectedObjectResult>{
        return objectDetector.analyze(
            image,
            imageRotation,
            imageCropPercentage,
            displaySize
        )
    }
}