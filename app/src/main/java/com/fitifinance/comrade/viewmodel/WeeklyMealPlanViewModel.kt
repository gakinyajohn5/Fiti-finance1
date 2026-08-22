package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.UserProfile
import com.fitifinance.comrade.engine.WeeklyMealPlan
import com.fitifinance.comrade.engine.WeeklyMealPlanEngine
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeeklyMealPlanState(
    val profile: UserProfile = UserProfile(),
    val plan: WeeklyMealPlan? = null
)

class WeeklyMealPlanViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _regenerateTick = MutableStateFlow(0L)

    val uiState: StateFlow<WeeklyMealPlanState> = combine(
        repository.observeProfileFlowSafe(),
        _regenerateTick
    ) { profile, tick ->
        WeeklyMealPlanState(
            profile = profile,
            plan = WeeklyMealPlanEngine.generate(profile, seed = tick.takeIf { it != 0L } ?: profile.hashCode().toLong())
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyMealPlanState())

    /** Reshuffles the week, keeping the same loved/hated/allergy rules. */
    fun regenerate() {
        viewModelScope.launch {
            _regenerateTick.value = System.currentTimeMillis()
        }
    }
}
