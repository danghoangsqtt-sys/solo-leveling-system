package com.systemleveling.feature.titles.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.TitleDao
import com.systemleveling.core.database.entity.TitleEntity
import com.systemleveling.core.engine.AchievementTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TitleViewModel @Inject constructor(
    private val titleDao: TitleDao,
    private val achievementTracker: AchievementTracker
) : ViewModel() {

    private val _titles = MutableStateFlow<List<TitleEntity>>(emptyList())
    val titles: StateFlow<List<TitleEntity>> = _titles.asStateFlow()

    private val _newlyUnlocked = MutableStateFlow<List<TitleEntity>>(emptyList())
    val newlyUnlocked: StateFlow<List<TitleEntity>> = _newlyUnlocked.asStateFlow()

    init {
        viewModelScope.launch {
            titleDao.getAllTitles().collect { dbTitles ->
                if (dbTitles.isEmpty()) {
                    withContext(Dispatchers.IO) {
                        achievementTracker.seedTitleDefinitions()
                    }
                } else {
                    _titles.value = dbTitles
                }
            }
        }
        // Check achievements on screen load
        checkAchievements()
    }

    fun checkAchievements() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val unlocked = achievementTracker.checkAllAchievements()
                if (unlocked.isNotEmpty()) {
                    _newlyUnlocked.value = unlocked
                }
            }
        }
    }

    fun equipTitle(titleId: String) {
        viewModelScope.launch {
            titleDao.unequipAll()
            titleDao.equipTitle(titleId)
        }
    }

    fun clearNewlyUnlocked() {
        _newlyUnlocked.value = emptyList()
    }
}
