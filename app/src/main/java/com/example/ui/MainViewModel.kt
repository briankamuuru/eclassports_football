package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TipCategory
import com.example.data.TipEntity
import com.example.data.TipRepository
import com.example.data.TipStatus
import com.example.ui.locale.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainSection {
    FREE_TIPS,
    VIP_TIPS,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TipRepository(application)

    // Current Main Section
    private val _currentSection = MutableStateFlow(MainSection.FREE_TIPS)
    val currentSection: StateFlow<MainSection> = _currentSection.asStateFlow()

    // VIP Navigation state
    private val _currentVipCategory = MutableStateFlow<TipCategory?>(null)
    val currentVipCategory: StateFlow<TipCategory?> = _currentVipCategory.asStateFlow()

    private val _isHistoryVipOpen = MutableStateFlow(false)
    val isHistoryVipOpen: StateFlow<Boolean> = _isHistoryVipOpen.asStateFlow()

    private val _currentHistoryCategory = MutableStateFlow<TipCategory?>(null)
    val currentHistoryCategory: StateFlow<TipCategory?> = _currentHistoryCategory.asStateFlow()

    // Language state
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Reactive tip lists
    val freeTips: StateFlow<List<TipEntity>> = repository.getAllTipsByCategory(TipCategory.FREE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vip2PlusActive: StateFlow<List<TipEntity>> = repository.getActiveTips(TipCategory.VIP_2_PLUS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vip3PlusActive: StateFlow<List<TipEntity>> = repository.getActiveTips(TipCategory.VIP_3_PLUS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vip5PlusActive: StateFlow<List<TipEntity>> = repository.getActiveTips(TipCategory.VIP_5_PLUS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vipSpecialActive: StateFlow<List<TipEntity>> = repository.getActiveTips(TipCategory.VIP_SPECIAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settled History tip lists (strictly independent)
    val vip2PlusHistory: StateFlow<List<TipEntity>> = repository.getSettledTips(TipCategory.VIP_2_PLUS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vip3PlusHistory: StateFlow<List<TipEntity>> = repository.getSettledTips(TipCategory.VIP_3_PLUS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vip5PlusHistory: StateFlow<List<TipEntity>> = repository.getSettledTips(TipCategory.VIP_5_PLUS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vipSpecialHistory: StateFlow<List<TipEntity>> = repository.getSettledTips(TipCategory.VIP_SPECIAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSection(section: MainSection) {
        _currentSection.value = section
        _currentVipCategory.value = null
        _isHistoryVipOpen.value = false
        _currentHistoryCategory.value = null
    }

    fun selectVipCategory(category: TipCategory) {
        _currentVipCategory.value = category
        _isHistoryVipOpen.value = false
        _currentHistoryCategory.value = null
    }

    fun openHistoryVip() {
        _isHistoryVipOpen.value = true
        _currentVipCategory.value = null
        _currentHistoryCategory.value = null
    }

    fun selectHistoryCategory(category: TipCategory) {
        _currentHistoryCategory.value = category
        _isHistoryVipOpen.value = false
    }

    fun navigateBack(): Boolean {
        if (_currentHistoryCategory.value != null) {
            _currentHistoryCategory.value = null
            _isHistoryVipOpen.value = true
            return true
        }
        if (_isHistoryVipOpen.value) {
            _isHistoryVipOpen.value = false
            return true
        }
        if (_currentVipCategory.value != null) {
            _currentVipCategory.value = null
            return true
        }
        if (_currentSection.value != MainSection.FREE_TIPS) {
            _currentSection.value = MainSection.FREE_TIPS
            return true
        }
        return false
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun settleTip(tipId: Long, status: TipStatus) {
        viewModelScope.launch {
            repository.settleTip(tipId, status)
        }
    }

    fun addTip(tip: TipEntity) {
        viewModelScope.launch {
            repository.addTip(tip)
        }
    }

    fun deleteTip(tip: TipEntity) {
        viewModelScope.launch {
            repository.deleteTip(tip)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDefaults()
        }
    }
}
