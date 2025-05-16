package com.example.travee.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class HomeViewModel : ViewModel() {
    // State for input fields
    private val _budgetInput = MutableStateFlow("")
    val budgetInput: StateFlow<String> = _budgetInput.asStateFlow()

    private val _startDate = MutableStateFlow<Calendar?>(null)
    val startDate: StateFlow<Calendar?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Calendar?>(null)
    val endDate: StateFlow<Calendar?> = _endDate.asStateFlow()

    private val _departureCity = MutableStateFlow("")
    val departureCity: StateFlow<String> = _departureCity.asStateFlow()

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Update methods
    fun updateBudget(budget: String) {
        _budgetInput.value = budget
    }

    fun updateStartDate(date: Calendar?) {
        _startDate.value = date
    }

    fun updateEndDate(date: Calendar?) {
        _endDate.value = date
    }

    fun clearDates() {
        _startDate.value = null
        _endDate.value = null
    }

    fun updateDepartureCity(city: String) {
        _departureCity.value = city
    }

    // Fetch user data from Firestore
    fun fetchUserData(auth: FirebaseAuth, db: FirebaseFirestore) {
        auth.currentUser?.uid?.let { userId ->
            try {
                db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val firstName = document.getString("firstName")
                            if (!firstName.isNullOrEmpty()) {
                                _userName.value = firstName
                                Log.d("HomeViewModel", "User name loaded: $firstName")
                            }
                        } else {
                            Log.d("HomeViewModel", "No user document found")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeViewModel", "Error getting user data", e)
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error retrieving user data", e)
            }
        }
    }
}
