package com.example.indoor_navigation.domain.tree

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion

sealed class TreeNode(
    val id: Int,
    var position: Float3,
    var neighbours: MutableList<Int> = mutableListOf()
) {
    class Path(
        id: Int,
        position: Float3,
        neighbours: MutableList<Int> = mutableListOf()
    ): TreeNode(id, position, neighbours) {

        fun copy(
            id: Int = this.id,
            position: Float3 = this.position,
            neighbours: MutableList<Int> = this.neighbours
        ): Path {
            return Path(
                id,
                position,
                neighbours
            )
        }
    }

    class Entry(
        var number: String,
        var forwardVector: Quaternion,
        id: Int,
        position: Float3,
        neighbours: MutableList<Int> = mutableListOf(),
    ): TreeNode(id, position, neighbours){

        fun copy(
            number: String = this.number,
            id: Int = this.id,
            position: Float3 = this.position,
            neighbours: MutableList<Int> = this.neighbours,
            forwardVector: Quaternion = this.forwardVector
        ): Entry {
            return Entry(
                number,
                forwardVector,
                id,
                position,
                neighbours,
            )
        }
    }
}