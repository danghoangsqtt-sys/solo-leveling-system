package com.systemleveling.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.FinanceDao
import com.systemleveling.core.database.dao.QuestDao
import com.systemleveling.core.database.dao.UserDao
import com.systemleveling.core.network.AiAvatarGeneratorService
import com.systemleveling.core.database.entity.StatEntity
import com.systemleveling.core.database.entity.UserEntity
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.ota.AppBuildInfo
import com.systemleveling.core.ota.OtaUpdateInfo
import com.systemleveling.core.ota.OtaUpdateManager
import com.systemleveling.core.settings.SettingsManager
import com.systemleveling.core.sync.CloudSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class SyncState { Idle, Restoring, Restored, Syncing, Synced, SyncFailed }

data class QuestSummary(val total: Int, val completed: Int)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDao: UserDao,
    questDao: QuestDao,
    financeDao: FinanceDao,
    private val settingsManager: SettingsManager,
    private val cloudSyncManager: CloudSyncManager,
    private val otaUpdateManager: OtaUpdateManager,
    private val buildInfo: AppBuildInfo,
    private val aiAvatarGeneratorService: AiAvatarGeneratorService
) : ViewModel() {

    private val todayStart: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    private val todayEnd: Long = todayStart + 86_400_000L

    private val _syncState = MutableStateFlow(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _otaUpdateInfo = MutableStateFlow<OtaUpdateInfo?>(null)
    val otaUpdateInfo: StateFlow<OtaUpdateInfo?> = _otaUpdateInfo.asStateFlow()

    private val _otaDownloading = MutableStateFlow(false)
    val otaDownloading: StateFlow<Boolean> = _otaDownloading.asStateFlow()

    private val _isGeneratingAvatar = MutableStateFlow(false)
    val isGeneratingAvatar: StateFlow<Boolean> = _isGeneratingAvatar.asStateFlow()

    private val _avatarError = MutableStateFlow<String?>(null)
    val avatarError: StateFlow<String?> = _avatarError.asStateFlow()

    init {
        viewModelScope.launch {
            _syncState.value = SyncState.Restoring
            val restored = cloudSyncManager.restoreIfEmpty()
            _syncState.value = if (restored) SyncState.Restored else SyncState.Idle
        }
        viewModelScope.launch {
            _otaUpdateInfo.value = otaUpdateManager.checkForUpdate(buildInfo.versionCode)
        }
    }

    fun dismissOtaUpdate() {
        _otaUpdateInfo.value = null
    }

    fun downloadAndInstallUpdate() {
        val url = _otaUpdateInfo.value?.downloadUrl ?: return
        viewModelScope.launch {
            _otaDownloading.value = true
            otaUpdateManager.downloadAndInstall(url)
            _otaDownloading.value = false
        }
    }

    fun pushToCloud() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val ok = cloudSyncManager.push()
            _syncState.value = if (ok) SyncState.Synced else SyncState.SyncFailed
        }
    }

    val user: StateFlow<UserEntity?> = userDao.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val stats: StateFlow<StatEntity?> = userDao.getStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val questSummary: StateFlow<QuestSummary> = questDao.getAllQuests()
        .map { quests ->
            QuestSummary(
                total = quests.size,
                completed = quests.count { it.status == QuestStatus.COMPLETED }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuestSummary(0, 0)
        )

    // True when ALL stats have reached the current stat cap — triggers class advancement
    val isAdvancementReady: StateFlow<Boolean> = combine(
        userDao.getUser(),
        userDao.getStats()
    ) { user, stats ->
        if (user == null || stats == null) return@combine false
        val cap = user.statCap
        stats.str >= cap && stats.intStat >= cap && stats.agi >= cap &&
            stats.vit >= cap && stats.wis >= cap && stats.cha >= cap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val geminiApiKey: StateFlow<String> = settingsManager.geminiApiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val supabaseUrl: StateFlow<String> = settingsManager.supabaseUrl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val supabaseAnonKey: StateFlow<String> = settingsManager.supabaseAnonKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val totalBalance: StateFlow<Long> = combine(
        financeDao.getTotalIncome(),
        financeDao.getTotalExpense()
    ) { income, expense -> (income ?: 0L) - (expense ?: 0L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayExpense: StateFlow<Long> = financeDao.getTodayExpense(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            settingsManager.setGeminiApiKey(key)
        }
    }

    fun saveSupabaseConfig(url: String, anonKey: String) {
        viewModelScope.launch {
            settingsManager.setSupabaseConfig(url, anonKey)
        }
    }

    fun generateAndSaveAvatar(profession: String, description: String) {
        viewModelScope.launch {
            _isGeneratingAvatar.value = true
            _avatarError.value = null
            val tier = userDao.getUserSync()?.promotionTier ?: 0
            val base64 = aiAvatarGeneratorService.generateAvatar(profession, description, tier)
            if (base64 != null) {
                userDao.updateAvatarProfile(profession.trim(), description.trim(), base64)
            } else {
                _avatarError.value = "Không thể tạo ảnh. Kiểm tra API key hoặc thử lại."
                userDao.updateAvatarProfile(profession.trim(), description.trim(), null)
            }
            _isGeneratingAvatar.value = false
        }
    }

    fun clearAvatarError() { _avatarError.value = null }
}
