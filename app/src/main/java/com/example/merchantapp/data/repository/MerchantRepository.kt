package com.example.merchantapp.data.repository

import android.content.Context
import android.util.Log
import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.model.LoginResponse
import com.example.merchantapp.model.MerchantProfileResponse
import com.example.merchantapp.model.MerchantRegistrationRequest
import com.example.merchantapp.model.MerchantRegistrationResponse
import com.example.merchantapp.network.ApiService // Ensure ApiService is imported
import com.example.merchantapp.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

// Result sealed class
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorMessage: String, val exception: Exception? = null) : Result<Nothing>()
}

class MerchantRepository(private val context: Context) {

    // Make apiService accessible only within this class, or ensure it's initialized correctly
    private val apiService: ApiService = RetrofitInstance.getApiService(context.applicationContext)

    suspend fun registerMerchant(request: MerchantRegistrationRequest): Result<MerchantRegistrationResponse> {
        return try {
            // Ensure 'registerMerchant' matches the method name in your ApiService.kt
            val response = apiService.registerMerchant(request) // CORRECTED CALL
            if (response.isSuccessful) {
                val responseBody = response.body()
                // The warnings "responseBody != null is always true" suggest an issue with nullability
                // or smart casting. Let's assume responseBody can indeed be null for robustness.
                if (responseBody != null) {
                    // Similarly, responseBody.error can be null.
                    if (responseBody.error != null) {
                        Log.w("MerchantRepository", "Registration response OK, but error in body: ${responseBody.error}")
                        Result.Error(responseBody.error!!) // Use !! if you're certain error is non-null when present
                    } else {
                        Result.Success(responseBody)
                    }
                } else {
                    Result.Error("Empty successful response body from server during registration.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("MerchantRepository", "Registration API Error ${response.code()}: $errorBody")
                var apiErrorMessage = "Registration failed. Please try again."
                if (!errorBody.isNullOrBlank()) {
                    try {
                        val errorResponse = Gson().fromJson(errorBody, MerchantRegistrationResponse::class.java)
                        if (errorResponse?.error != null) {
                            apiErrorMessage = errorResponse.error!!
                        } else {
                            apiErrorMessage = errorBody // Use full errorBody if specific error field not found
                        }
                    } catch (e: JsonSyntaxException) {
                        Log.e("MerchantRepository", "Error parsing registration error JSON: $e")
                        apiErrorMessage = errorBody ?: "Unknown error structure."
                    }
                }
                Result.Error(apiErrorMessage)
            }
        } catch (e: Exception) {
            Log.e("MerchantRepository", "Exception during merchant registration: ${e.message}", e)
            Result.Error("Network error or other exception during registration: ${e.message ?: "Unknown error"}", e)
        }
    }

    suspend fun loginMerchant(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d("MerchantRepository", "Attempting login for: ${request.email}")
            // Ensure 'loginMerchant' matches the method name in your ApiService.kt
            val response = apiService.loginMerchant(request) // CORRECTED CALL

            if (response.isSuccessful) {
                val loginResponseBody = response.body()
                if (loginResponseBody != null) {
                    if (loginResponseBody.token != null) {
                        Log.i("MerchantRepository", "Login successful for ${request.email}. Token received.")
                        Result.Success(loginResponseBody)
                    } else if (loginResponseBody.error != null) {
                        Log.w("MerchantRepository", "Login response OK, but error in body: ${loginResponseBody.error}")
                        Result.Error(loginResponseBody.error!!)
                    } else {
                        Log.e("MerchantRepository", "Login response OK, but token and error are null. Response: $loginResponseBody")
                        Result.Error("Login response malformed (missing token and error).")
                    }
                } else {
                    Result.Error("Empty successful response body from server during login.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("MerchantRepository", "Login API Error ${response.code()}: $errorBody")
                var apiErrorMessage = "Login failed. Please try again."
                if (!errorBody.isNullOrBlank()) {
                    try {
                        val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
                        if (errorResponse?.error != null) {
                            apiErrorMessage = errorResponse.error!!
                        } else {
                            apiErrorMessage = errorBody
                        }
                    } catch (e: JsonSyntaxException) {
                        Log.e("MerchantRepository", "Error parsing login error JSON: $e")
                        apiErrorMessage = errorBody ?: "Unknown error structure."
                    }
                }
                Result.Error(apiErrorMessage)
            }
        } catch (e: Exception) {
            Log.e("MerchantRepository", "Exception during merchant login: ${e.message}", e)
            Result.Error("Network error or other exception during login: ${e.message ?: "Unknown error"}", e)
        }
    }

    suspend fun getMerchantProfile(): Result<MerchantProfileResponse> {
        return try {
            Log.d("MerchantRepository", "Fetching merchant profile...")
            // Ensure 'fetchMerchantProfile' (or similar) matches the method name in your ApiService.kt
            val response = apiService.getMerchantProfile() // CORRECTED CALL

            if (response.isSuccessful) {
                val profileResponseBody = response.body()
                if (profileResponseBody != null) {
                    // Assuming MerchantProfileResponse itself is the data or has an error field.
                    // If your MerchantProfileResponse has an "error" field for business logic errors on 200 OK:
                    // if (profileResponseBody.error != null) {
                    //    Log.w("MerchantRepository", "Get profile response OK, but error in body: ${profileResponseBody.error}")
                    //    return Result.Error(profileResponseBody.error!!)
                    // }
                    Log.i("MerchantRepository", "Merchant profile fetched successfully.")
                    Result.Success(profileResponseBody)
                } else {
                    Result.Error("Empty successful response body when fetching profile.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("MerchantRepository", "Get profile API Error ${response.code()}: $errorBody")
                var apiErrorMessage = "Failed to fetch profile. Please try again."
                if (!errorBody.isNullOrBlank()) {
                    try {
                        // Attempt to parse a generic error structure like {"error": "message"}
                        // Using a simple Map here instead of MerchantProfileResponse for more generic error parsing
                        data class GenericErrorResponse(val error: String?)
                        val errorResponse = Gson().fromJson(errorBody, GenericErrorResponse::class.java)
                        if (errorResponse?.error != null) {
                            apiErrorMessage = errorResponse.error!!
                        } else {
                            apiErrorMessage = errorBody // Fallback to raw error body
                        }
                    } catch (e: JsonSyntaxException) {
                        Log.e("MerchantRepository", "Error parsing get profile error JSON: $e")
                        apiErrorMessage = errorBody ?: "Unknown error structure."
                    }
                }
                if (response.code() == 401) {
                    // It's good practice to provide a more specific message for 401
                    // and potentially trigger a global logout or token refresh.
                    apiErrorMessage = "Unauthorized. Your session may have expired. Please login again."
                    // Example: AuthManager.logout(context.applicationContext) // If you decide to auto-logout
                }
                Result.Error(apiErrorMessage)
            }
        } catch (e: Exception) {
            Log.e("MerchantRepository", "Exception during get merchant profile: ${e.message}", e)
            Result.Error("Network error or other exception fetching profile: ${e.message ?: "Unknown error"}", e)
        }
    }
}