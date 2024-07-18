package com.example.indoor_navigation.domain.repository

import com.example.indoor_navigation.data.model.Record
import kotlinx.coroutines.flow.Flow

interface RecordsRepository {

    suspend fun insertRecord(record: Record)

    fun getRecords(time: Long, limit: Int): Flow<List<Record>>

    fun getRecords(): Flow<List<Record>>

}