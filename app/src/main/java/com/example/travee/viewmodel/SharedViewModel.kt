package com.example.travee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A shared ViewModel to maintain state across different screens
 */
class SharedViewModel : ViewModel() {

    // Home screen state
    private val _homeScrollPosition = MutableStateFlow(0)
    val homeScrollPosition: StateFlow<Int> = _homeScrollPosition.asStateFlow()

    // Flight details screen state
    private val _flightDetailsScrollPosition = MutableStateFlow(0)
    val flightDetailsScrollPosition: StateFlow<Int> = _flightDetailsScrollPosition.asStateFlow()

    // Favorites screen state
    private val _favoritesScrollPosition = MutableStateFlow(0)
    val favoritesScrollPosition: StateFlow<Int> = _favoritesScrollPosition.asStateFlow()

    // Profile screen state
    private val _profileScrollPosition = MutableStateFlow(0)
    val profileScrollPosition: StateFlow<Int> = _profileScrollPosition.asStateFlow()

    // Update scroll positions
    fun updateHomeScrollPosition(position: Int) {
        viewModelScope.launch {
            _homeScrollPosition.value = position
        }
    }

    fun updateFlightDetailsScrollPosition(position: Int) {
        viewModelScope.launch {
            _flightDetailsScrollPosition.value = position
        }
    }

    fun updateFavoritesScrollPosition(position: Int) {
        viewModelScope.launch {
            _favoritesScrollPosition.value = position
        }
    }

    fun updateProfileScrollPosition(position: Int) {
        viewModelScope.launch {
            _profileScrollPosition.value = position
        }
    }
}
