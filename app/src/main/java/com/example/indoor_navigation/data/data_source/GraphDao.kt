package com.example.indoor_navigation.data.data_source

import androidx.room.*
import com.example.indoor_navigation.data.model.TreeNodeDto

@Dao
interface GraphDao {

    @Query("SELECT * FROM treenodedto")
    fun getNodes(): List<TreeNodeDto>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNodes(nodes: List<TreeNodeDto>)

    @Delete
    fun deleteNodes(nodes: List<TreeNodeDto>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNodes(nodes: List<TreeNodeDto>)

    @Query("DELETE FROM treenodedto")
    fun clearNodes()

}