// NEW FILE: app/src/main/java/com/example/merchantapp/viewmodel/MainViewModel.kt
package com.example.merchantapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.data.repository.MerchantRepository
import com.example.merchantapp.data.repository.Result // Ensure this is your Result class
import com.example.merchantapp.model.MerchantProfileResponse // Your profile data model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoadingProfile: Boolean = false,
    val merchantProfile: MerchantProfileResponse? = null,
    val profileErrorMessage: String? = null
    // Add other states for your MainScreen as needed (e.g., list of transactions)
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val merchantRepository: MerchantRepository = MerchantRepository(application.applicationContext)

    // Call this function to load the profile, e.g., when MainScreen is first displayed
    fun fetchMerchantProfile() {
        if (_uiState.value.isLoadingProfile || _uiState.value.merchantProfile != null) {
            // Avoid refetching if already loading or profile already loaded
            // You might want a refresh mechanism later
            Log.d("MainViewModel", "Profile already loading or loaded. Skipping fetch.")
            return
        }

        _uiState.update { it.copy(isLoadingProfile = true, profileErrorMessage = null) }
        Log.d("MainViewModel", "Fetching merchant profile...")

        viewModelScope.launch {
            when (val result = merchantRepository.getMerchantProfile()) {
                is Result.Success -> {
                    Log.i("MainViewModel", "Merchant profile fetched successfully: ${result.data.businessName}")
                    _uiState.update {
                        it.copy(
                            isLoadingProfile = false,
                            merchantProfile = result.data,
                            profileErrorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    Log.e("MainViewModel", "Failed to fetch merchant profile: ${result.errorMessage}")
                    _uiState.update {
                        it.copy(
                            isLoadingProfile = false,
                            profileErrorMessage = result.errorMessage
                        )
                    }
                }
            }
        }
    }

    // Call this if you want to allow manual refresh of the profile
    fun refreshMerchantProfile() {
        _uiState.update { it.copy(merchantProfile = null) } // Clear old profile to allow refetch
        fetchMerchantProfile()
    }
}