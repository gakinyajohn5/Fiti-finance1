package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.*
import com.fitifinance.comrade.engine.AllergenTag
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Drives the 5-step Comrade Persona Onboarding Wizard. */
class OnboardingViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _step = MutableStateFlow(1)
    val step: StateFlow<Int> = _step.asStateFlow()

    val totalSteps = 5

    fun setLivingSituation(v: LivingSituation) {
        _profile.value = _profile.value.copy(livingSituation = v)
    }

    fun setRoommateStatus(v: RoommateStatus) {
        _profile.value = _profile.value.copy(roommateStatus = v)
    }

    fun setMealStyle(v: MealStyle) {
        _profile.value = _profile.value.copy(mealStyle = v)
    }

    fun setPartyStatus(v: PartyStatus) {
        _profile.value = _profile.value.copy(
            partyStatus = v,
            partyFrequency = if (v == PartyStatus.NEVER) PartyFrequency.NOT_APPLICABLE else _profile.value.partyFrequency
        )
    }

    fun setPartyFrequency(v: PartyFrequency) {
        _profile.value = _profile.value.copy(partyFrequency = v)
    }

    fun toggleFavoriteDrink(drinkId: String) {
        val current = _profile.value.favoriteDrinkIds
            .split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
        if (!current.add(drinkId)) current.remove(drinkId)
        _profile.value = _profile.value.copy(favoriteDrinkIds = current.joinToString(","))
    }

    fun toggleLovedFood(foodId: String) {
        val current = _profile.value.lovedFoodIdSet.toMutableSet()
        if (!current.add(foodId)) current.remove(foodId)
        // Loving and hating the same item at once makes no sense — clear the other list.
        val hated = _profile.value.hatedFoodIdSet - foodId
        _profile.value = _profile.value.copy(lovedFoodIds = current.joinToString(","), hatedFoodIds = hated.joinToString(","))
    }

    fun toggleHatedFood(foodId: String) {
        val current = _profile.value.hatedFoodIdSet.toMutableSet()
        if (!current.add(foodId)) current.remove(foodId)
        val loved = _profile.value.lovedFoodIdSet - foodId
        _profile.value = _profile.value.copy(hatedFoodIds = current.joinToString(","), lovedFoodIds = loved.joinToString(","))
    }

    fun toggleAllergy(tag: AllergenTag) {
        val current = _profile.value.allergenSet.map { it.name }.toMutableSet()
        if (!current.add(tag.name)) current.remove(tag.name)
        _profile.value = _profile.value.copy(allergyTags = current.joinToString(","))
    }

    fun nextStep() {
        // Skip party frequency/drinks sub-step entirely if user never drinks.
        _step.value = (_step.value + 1).coerceAtMost(totalSteps)
    }

    fun previousStep() {
        _step.value = (_step.value - 1).coerceAtLeast(1)
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.saveProfile(_profile.value.copy(onboardingComplete = true))
            onDone()
        }
    }
}
