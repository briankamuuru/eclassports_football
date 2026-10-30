package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TipRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val tipDao = database.tipDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (tipDao.getCount() == 0) {
                tipDao.insertAll(AppDatabase.getInitialSeedTips())
            }
        }
    }

    fun getActiveTips(category: TipCategory): Flow<List<TipEntity>> {
        return tipDao.getActiveTips(category.id)
    }

    fun getSettledTips(category: TipCategory): Flow<List<TipEntity>> {
        return tipDao.getSettledTips(category.id)
    }

    fun getAllTipsByCategory(category: TipCategory): Flow<List<TipEntity>> {
        return tipDao.getAllTipsByCategory(category.id)
    }

    suspend fun settleTip(tipId: Long, newStatus: TipStatus) {
        val tip = tipDao.getTipById(tipId) ?: return
        tipDao.updateTip(tip.copy(status = newStatus.name))
    }

    suspend fun addTip(tip: TipEntity): Long {
        return tipDao.insertTip(tip)
    }

    suspend fun updateTip(tip: TipEntity) {
        tipDao.updateTip(tip)
    }

    suspend fun deleteTip(tip: TipEntity) {
        tipDao.deleteTip(tip)
    }

    suspend fun resetDefaults() {
        tipDao.clearAll()
        tipDao.insertAll(AppDatabase.getInitialSeedTips())
    }
}
