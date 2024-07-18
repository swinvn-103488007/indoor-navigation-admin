package com.example.indoor_navigation.domain.use_cases

import com.example.indoor_navigation.domain.pathfinding.Path
import com.example.indoor_navigation.domain.pathfinding.Pathfinder
import com.example.indoor_navigation.domain.tree.Tree
import com.example.indoor_navigation.domain.tree.TreeNode

class FindWay(
    private val pathfinder: Pathfinder
) {

    suspend operator fun invoke(
        from: String,
        to: String,
        tree: Tree
    ): Path? {
        return pathfinder.findWay(from, to, tree)
    }
}