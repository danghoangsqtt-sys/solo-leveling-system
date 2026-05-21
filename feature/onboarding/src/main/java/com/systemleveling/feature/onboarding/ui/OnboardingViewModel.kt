package com.systemleveling.feature.onboarding.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userDao: UserDao,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    fun completeOnboarding(
        nickname: String,
        selectedClass: String,
        surveyStats: StatEntity? = null
    ) {
        viewModelScope.launch {
            userDao.insertUser(
                UserEntity(
                    nickname = nickname.ifBlank { "Shadow Monarch" },
                    characterClass = selectedClass,
                    avatarUri = null
                )
            )

            // If survey computed stats exist, apply class bonus on top
            // Otherwise fall back to fixed defaults
            val stats = if (surveyStats != null) {
                applyClassBonus(surveyStats, selectedClass)
            } else {
                when (selectedClass) {
                    "Warrior" -> StatEntity(str = 20, intStat = 8, agi = 10, vit = 18, wis = 8, cha = 10)
                    "Mage"    -> StatEntity(str = 8, intStat = 20, agi = 10, vit = 8, wis = 18, cha = 12)
                    "Ranger"  -> StatEntity(str = 10, intStat = 10, agi = 20, vit = 10, wis = 10, cha = 16)
                    else      -> StatEntity()
                }
            }
            userDao.insertStats(stats)

            val isOnboardedKey = booleanPreferencesKey("isOnboarded")
            dataStore.edit { settings -> settings[isOnboardedKey] = true }
        }
    }

    private fun applyClassBonus(base: StatEntity, selectedClass: String): StatEntity =
        when (selectedClass) {
            "Warrior" -> base.copy(
                str = (base.str + 10).coerceAtMost(100),
                vit = (base.vit + 8).coerceAtMost(100)
            )
            "Mage" -> base.copy(
                intStat = (base.intStat + 10).coerceAtMost(100),
                wis = (base.wis + 8).coerceAtMost(100)
            )
            "Ranger" -> base.copy(
                agi = (base.agi + 10).coerceAtMost(100),
                cha = (base.cha + 8).coerceAtMost(100)
            )
            else -> base
        }
}
