package com.example.travee.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.example.travee.TravelApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ThemeManager private constructor() {
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun initialize(context: Context) {
        // First load from SharedPreferences for immediate access
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isDarkTheme.value = sharedPrefs.getBoolean(KEY_DARK_MODE, false)

        // Then try to load from Firebase if user is logged in
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            loadThemePreferenceFromFirestore(auth.currentUser!!.uid)
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        saveThemePreference()
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        saveThemePreference()
    }

    private fun saveThemePreference() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Save to SharedPreferences for immediate access next time
        val context = TravelApp.appContext
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_DARK_MODE, _isDarkTheme.value).apply()

        // Save to Firestore if user is logged in
        if (auth.currentUser != null) {
            coroutineScope.launch {
                try {
                    db.collection("users")
                        .document(auth.currentUser!!.uid)
                        .update("darkModeEnabled", _isDarkTheme.value)
                } catch (e: Exception) {
                    // Ignore errors, the preference is already saved locally
                }
            }
        }
    }

    private fun loadThemePreferenceFromFirestore(userId: String) {
        val db = FirebaseFirestore.getInstance()

        coroutineScope.launch {
            try {
                val document = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (document.exists() && document.contains("darkModeEnabled")) {
                    val darkModeEnabled = document.getBoolean("darkModeEnabled") ?: false
                    _isDarkTheme.value = darkModeEnabled

                    // Update SharedPreferences
                    val context = TravelApp.appContext
                    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean(KEY_DARK_MODE, darkModeEnabled).apply()
                }
            } catch (e: Exception) {
                // Ignore errors, we'll use the local preference
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "travel_app_prefs"
        private const val KEY_DARK_MODE = "dark_mode"

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager().also { instance = it }
            }
        }
    }
}
