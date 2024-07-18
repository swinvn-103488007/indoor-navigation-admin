package com.example.indoor_navigation.presentation.preview.state

import com.example.indoor_navigation.domain.pathfinding.Path
import com.example.indoor_navigation.domain.tree.TreeNode
import com.example.indoor_navigation.presentation.LabelObject

data class PathState(
    val startEntry: TreeNode.Entry? = null,
    val endEntry: TreeNode.Entry? = null,
    val path: Path? = null
)
