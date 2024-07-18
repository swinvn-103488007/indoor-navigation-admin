package com.example.indoor_navigation.domain.pathfinding

import com.example.indoor_navigation.domain.tree.Tree
import com.example.indoor_navigation.domain.tree.TreeNode

interface Pathfinder {

    suspend fun findWay(
        from: String,
        to: String,
        tree: Tree
    ): Path?

}