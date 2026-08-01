package com.example.data

import kotlinx.coroutines.flow.Flow

class PdfHistoryRepository(private val dao: PdfHistoryDao) {
    val allHistory: Flow<List<PdfHistoryEntity>> = dao.getAllHistory()

    suspend fun save(entity: PdfHistoryEntity): Long = dao.insertHistory(entity)
    suspend fun delete(id: Long) = dao.deleteHistoryById(id)
    suspend fun clear() = dao.clearAllHistory()
}
