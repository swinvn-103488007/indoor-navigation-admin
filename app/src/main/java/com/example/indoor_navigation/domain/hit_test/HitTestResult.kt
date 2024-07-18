package com.example.indoor_navigation.domain.hit_test

import com.google.ar.core.HitResult

data class HitTestResult(
    val orientatedPosition: OrientatedPosition,
    val hitResult: HitResult
) {
}