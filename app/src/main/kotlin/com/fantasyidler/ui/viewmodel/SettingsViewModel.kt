package com.fantasyidler.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fantasyidler.data.model.PlayerFlags
import com.fantasyidler.repository.BackupScheduler
import com.fantasyidler.repository.PlayerRepository
import com.fantasyidler.repository.QueuedSessionStarter
import com.fantasyidler.repository.QuestRepository
import com.fantasyidler.repository.SessionRepository
import com.fantasyidler.repository.WorkerQueuedSessionStarter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepo: PlayerRepository,
    private val sessionRepo: SessionRepository,
    private val questRepo: QuestRepository,
    private val queuedSessionStarter: QueuedSessionStarter,
    private val workerStarter: WorkerQueuedSessionStarter,
    private val backupScheduler: BackupScheduler,
    private val json: Json,
) : ViewModel() {

    val themePreference: StateFlow<String> = playerRepo.playerFlow
        .map { player ->
            if (player == null) return@map "dark"
            try { json.decodeFromString<PlayerFlags>(player.flags).themePreference }
            catch (_: Exception) { "dark" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "dark")

    val fontScale: StateFlow<Float> = playerRepo.playerFlow
        .map { player ->
            if (player == null) return@map 1.0f
            try { json.decodeFromString<PlayerFlags>(player.flags).fontScale }
            catch (_: Exception) { 1.0f }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1.0f)

    val backupFolderUri: StateFlow<String> = playerRepo.playerFlow
        .map { player ->
            if (player == null) return@map ""
            try { json.decodeFromString<PlayerFlags>(player.flags).backupFolderUri }
            catch (_: Exception) { "" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val backupFrequency: StateFlow<String> = playerRepo.playerFlow
        .map { player ->
            if (player == null) return@map ""
            try { json.decodeFromString<PlayerFlags>(player.flags).backupFrequency }
            catch (_: Exception) { "" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setTheme(preference: String) {
        viewModelScope.launch {
            val flags = playerRepo.getFlags()
            playerRepo.updateFlags(flags.copy(themePreference = preference))
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            val flags = playerRepo.getFlags()
            playerRepo.updateFlags(flags.copy(fontScale = scale))
        }
    }

    fun setBackupFolder(uriString: String) {
        viewModelScope.launch {
            val flags = playerRepo.getFlags()
            val permFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            if (flags.backupFolderUri.isNotEmpty()) {
                try { context.contentResolver.releasePersistableUriPermission(Uri.parse(flags.backupFolderUri), permFlags) }
                catch (_: Exception) {}
            }
            context.contentResolver.takePersistableUriPermission(Uri.parse(uriString), permFlags)
            playerRepo.updateFlags(flags.copy(backupFolderUri = uriString))
            if (flags.backupFrequency.isNotEmpty()) backupScheduler.schedule(flags.backupFrequency)
        }
    }

    fun setBackupFrequency(frequency: String) {
        viewModelScope.launch {
            val flags = playerRepo.getFlags()
            playerRepo.updateFlags(flags.copy(backupFrequency = frequency))
            backupScheduler.schedule(frequency)
        }
    }

    fun backupNow(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupScheduler.performBackup(playerRepo)
            onDone(success)
        }
    }

    fun resetProgression() {
        viewModelScope.launch {
            sessionRepo.deleteAllSessions()
            questRepo.resetAllProgress()
            playerRepo.resetProgression()
        }
    }

    fun exportSave(onReady: (String) -> Unit) {
        viewModelScope.launch {
            onReady(playerRepo.exportSave())
        }
    }

    fun importSave(jsonString: String, onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                playerRepo.importSave(jsonString)
                sessionRepo.deleteAllSessions()
                sessionRepo.deleteAllWorkerSessions()
                sessionRepo.recoverActiveSession(queuedSessionStarter)
                sessionRepo.recoverActiveWorkerSession(workerStarter)
                onDone(true)
            } catch (_: Exception) {
                onDone(false)
            }
        }
    }
}
