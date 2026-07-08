package com.fantasyidler.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fantasyidler.R
import com.fantasyidler.data.model.HiredWorker
import com.fantasyidler.data.model.PlayerFlags
import com.fantasyidler.data.model.WorkerTier
import com.fantasyidler.repository.GameDataRepository
import com.fantasyidler.repository.PlayerRepository
import com.fantasyidler.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

data class DailyFoodItem(
    val key: String,
    val displayName: String,
    val healValue: Int,
    val price: Int,
)

data class InnUiState(
    val isLoading: Boolean = true,
    val coins: Long = 0L,
    val hiredWorker: HiredWorker? = null,
    val longLaborerName: String = "",
    val apprenticeName: String = "",
    val journeymanName: String = "",
    val masterName: String = "",
    val dailyFoods: List<DailyFoodItem> = emptyList(),
    val snackbarMessage: String? = null,
    val navigateToWorkerSkills: Boolean = false,
)

@HiltViewModel
class InnViewModel @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val sessionRepo: SessionRepository,
    private val gameData: GameDataRepository,
    @ApplicationContext private val context: Context,
    private val json: Json,
) : ViewModel() {

    private val _extra = MutableStateFlow(InnUiState(dailyFoods = computeDailyFoods()))

    val uiState: StateFlow<InnUiState> = combine(
        playerRepo.playerFlow,
        _extra,
    ) { player, extra ->
        if (player == null) extra.copy(isLoading = true)
        else {
            val flags: PlayerFlags = json.decodeFromString(player.flags)
            extra.copy(
                isLoading        = false,
                coins            = player.coins,
                hiredWorker      = flags.hiredWorker,
                longLaborerName  = workerName(WorkerTier.LONG_LABORER),
                apprenticeName   = workerName(WorkerTier.APPRENTICE),
                journeymanName   = workerName(WorkerTier.JOURNEYMAN),
                masterName       = workerName(WorkerTier.MASTER),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InnUiState())

    fun hire(tier: WorkerTier) {
        viewModelScope.launch {
            val flags = playerRepo.getFlags()
            if (flags.hiredWorker != null) {
                _extra.update { it.copy(snackbarMessage = context.getString(R.string.inn_worker_already_hired)) }
                return@launch
            }
            val name = workerName(tier)
            if (!playerRepo.spendCoins(tier.hireCost)) {
                _extra.update { it.copy(snackbarMessage = context.getString(R.string.inn_not_enough_coins)) }
                return@launch
            }
            sessionRepo.deleteAllWorkerSessions()
            playerRepo.updateFlags(flags.copy(hiredWorker = HiredWorker(tier, name)))
            _extra.update { it.copy(navigateToWorkerSkills = true) }
        }
    }

    fun buyFood(key: String, price: Int) {
        viewModelScope.launch {
            val success = playerRepo.buyItem(key, 1, price)
            _extra.update {
                it.copy(
                    snackbarMessage = if (success) context.getString(R.string.inn_food_purchased)
                                      else context.getString(R.string.inn_not_enough_coins)
                )
            }
        }
    }

    fun navigationHandled() = _extra.update { it.copy(navigateToWorkerSkills = false) }
    fun snackbarConsumed() = _extra.update { it.copy(snackbarMessage = null) }

    private fun workerName(tier: WorkerTier): String {
        val names = context.resources.getStringArray(R.array.worker_names)
        if (names.isEmpty()) return "Worker"
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.HOUR_OF_DAY) < 6) cal.add(Calendar.DAY_OF_YEAR, -1)
        val daySeed = cal.get(Calendar.YEAR) * 10000L + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH)
        val rng = Random(daySeed + tier.ordinal * 7919L)
        return names[rng.nextInt(names.size)]
    }

    private fun computeDailyFoods(): List<DailyFoodItem> {
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.HOUR_OF_DAY) < 6) cal.add(Calendar.DAY_OF_YEAR, -1)
        val seed = cal.get(Calendar.YEAR) * 10000L + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH) + 54321L
        val rng = Random(seed)

        val recipes = gameData.cookingRecipes.values
        val small  = recipes.filter { it.healingValue in 1..7 }
        val medium = recipes.filter { it.healingValue in 8..14 }
        val large  = recipes.filter { it.healingValue >= 15 }

        return listOfNotNull(
            small.randomOrNull(rng)?.let  { DailyFoodItem(it.cookedItem, it.displayName, it.healingValue, 100) },
            medium.randomOrNull(rng)?.let { DailyFoodItem(it.cookedItem, it.displayName, it.healingValue, 250) },
            large.randomOrNull(rng)?.let  { DailyFoodItem(it.cookedItem, it.displayName, it.healingValue, 500) },
        )
    }
}
