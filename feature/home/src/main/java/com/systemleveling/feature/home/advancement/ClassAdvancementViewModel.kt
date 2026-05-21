package com.systemleveling.feature.home.advancement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdvancementState(
    val user: UserEntity? = null,
    val stats: StatEntity? = null,
    val newClass: String = "",
    val newTier: Int = 0,
    val newStatCap: Int = 150
)

@HiltViewModel
class ClassAdvancementViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    val state: StateFlow<AdvancementState> = combine(
        userDao.getUser(),
        userDao.getStats()
    ) { user, stats ->
        val tier = user?.promotionTier ?: 0
        val cap = nextStatCap(tier)
        AdvancementState(
            user = user,
            stats = stats,
            newClass = resolvePromotedClass(user?.characterClass ?: "Warrior", tier),
            newTier = tier + 1,
            newStatCap = cap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdvancementState())

    fun confirmAdvancement() {
        val current = state.value.user ?: return
        viewModelScope.launch {
            userDao.updatePromotion(
                tier = current.promotionTier + 1,
                cap = nextStatCap(current.promotionTier)
            )
        }
    }

    companion object {
        fun nextStatCap(currentTier: Int): Int = when (currentTier) {
            0 -> 150
            1 -> 200
            else -> 200 + (currentTier - 1) * 50
        }

        fun resolvePromotedClass(base: String, currentTier: Int): String {
            return when (base) {
                "Warrior" -> when (currentTier) {
                    0 -> "Elite Warrior"
                    1 -> "Shadow Monarch"
                    else -> "Demonic Sovereign"
                }
                "Mage" -> when (currentTier) {
                    0 -> "Arch Mage"
                    1 -> "Arcane Alchemist"
                    else -> "Void Sorcerer"
                }
                "Ranger" -> when (currentTier) {
                    0 -> "Shadow Ranger"
                    1 -> "Phantom Assassin"
                    else -> "Void Hunter"
                }
                else -> "Ascended $base"
            }
        }

        fun classPrimaryTrait(newClass: String): String = when (newClass) {
            "Elite Warrior"    -> "Vanguard Dominance"
            "Shadow Monarch"   -> "Army of the Dead"
            "Demonic Sovereign"-> "Absolute Power"
            "Arch Mage"        -> "Mana Amplification"
            "Arcane Alchemist" -> "Matter Manipulation"
            "Void Sorcerer"    -> "Reality Fracture"
            "Shadow Ranger"    -> "Lethal Precision"
            "Phantom Assassin" -> "Shadow Step"
            "Void Hunter"      -> "Dimensional Rift"
            else               -> "Enhanced Awakening"
        }

        fun classStartingSkill(newClass: String): String = when (newClass) {
            "Elite Warrior"    -> "Iron Fortress"
            "Shadow Monarch"   -> "Arise"
            "Arch Mage"        -> "Mana Burst"
            "Arcane Alchemist" -> "Equivalent Exchange"
            "Shadow Ranger"    -> "Shadow Shot"
            "Phantom Assassin" -> "Void Step"
            else               -> "Power Surge"
        }

        fun classTierLabel(tier: Int): String = when (tier) {
            1 -> "Advanced Class"
            2 -> "Legendary Class"
            else -> "Mythic Class"
        }
    }
}
