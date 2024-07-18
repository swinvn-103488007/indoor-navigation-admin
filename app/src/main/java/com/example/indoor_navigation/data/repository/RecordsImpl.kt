package com.example.indoor_navigation.data.repository

import com.example.indoor_navigation.data.data_source.Database
import com.example.indoor_navigation.data.model.Record
import com.example.indoor_navigation.domain.repository.RecordsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecordsImpl @Inject constructor(
    private val database: Database
): RecordsRepository {

    private val dao = database.recordsDao

    override suspend fun insertRecord(record: Record) {
        dao.insertRecord(record)
    }

    override fun getRecords(time: Long, limit: Int): Flow<List<Record>> {
        return dao.getRecords(time, limit)
    }

    override fun getRecords(): Flow<List<Record>> {
        return dao.getRecords()
    }
}