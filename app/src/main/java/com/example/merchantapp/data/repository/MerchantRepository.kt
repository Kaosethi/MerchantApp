// MODIFIED: app/src/main/java/com/example/merchantapp/data/repository/MerchantRepository.kt
package com.example.merchantapp.data.repository

import android.content.Context
import android.util.Log
import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.model.LoginResponse
import com.example.merchantapp.model.MerchantProfileResponse // ADDED: Import for the response type
import com.example.merchantapp.model.MerchantRegistrationRequest
import com.example.merchantapp.model.MerchantRegistrationResponse
import com.example.merchantapp.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

// Result sealed class (ensure this is the single source of truth or imported)
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorMessage: String, val exception: Exception? = null) : Result<Nothing>()
}

class MerchantRepository(private val context: Context) {

    private val apiService = RetrofitInstance.getApiService(context.applicationContext)

    // registerMerchant function remains the same...
    suspend fun registerMerchant(request: MerchantRegistrationRequest): Result<MerchantRegistrationResponse> {
        return try {
            val response = apiService.merchantRegister(request)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.error != null) {
                        Log.w("MerchantRepository", "Registration response OK, but error in body: ${responseBody.error}")
                        Result.Error(responseBody.error)
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
                            apiErrorMessage = errorResponse.error
                        } else {
                            apiErrorMessage = errorBody
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


    // loginMerchant function remains the same...
    suspend fun loginMerchant(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d("MerchantRepository", "Attempting login for: ${request.email}")
            val response = apiService.merchantLogin(request)

            if (response.isSuccessful) {
                val loginResponseBody = response.body()
                if (loginResponseBody != null) {
                    if (loginResponseBody.token != null) {
                        Log.i("MerchantRepository", "Login successful for ${request.email}. Token received.")
                        Result.Success(loginResponseBody)
                    } else if (loginResponseBody.error != null) {
                        Log.w("MerchantRepository", "Login response OK, but error in body: ${loginResponseBody.error}")
                        Result.Error(loginResponseBody.error)
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
                            apiErrorMessage = errorResponse.error
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

    // --- ADDED: getMerchantProfile function ---
    suspend fun getMerchantProfile(): Result<MerchantProfileResponse> {
        return try {
            Log.d("MerchantRepository", "Fetching merchant profile...")
            val response = apiService.getMerchantProfile() // AuthInterceptor will add the token

            if (response.isSuccessful) {
                val profileResponseBody = response.body()
                if (profileResponseBody != null) {
                    // Check for a business logic error field if your /profile endpoint might return one on 200 OK
                    // For a simple GET profile, usually if it's 200 OK, the data is good.
                    // If MerchantProfileResponse has an 'error' field for such cases:
                    // if (profileResponseBody.error != null) {
                    // Log.w("MerchantRepository", "Get profile response OK, but error in body: ${profileResponseBody.error}")
                    // return Result.Error(profileResponseBody.error)
                    // }
                    Log.i("MerchantRepository", "Merchant profile fetched successfully.")
                    Result.Success(profileResponseBody)
                } else {
                    Result.Error("Empty successful response body when fetching profile.")
                }
            } else {
                // Handle 401 Unauthorized (e.g., bad/expired token), 404 Not Found, 5xx Server Error, etc.
                val errorBody = response.errorBody()?.string()
                Log.e("MerchantRepository", "Get profile API Error ${response.code()}: $errorBody")
                var apiErrorMessage = "Failed to fetch profile. Please try again."
                if (!errorBody.isNullOrBlank()) {
                    try {
                        // Assuming error response from /profile also has structure {"error": "message"}
                        // We use MerchantProfileResponse to attempt parsing, as it might have an 'error' field
                        val errorResponse = Gson().fromJson(errorBody, MerchantProfileResponse::class.java)
                        if (errorResponse?.error != null) {
                            apiErrorMessage = errorResponse.error
                        } else {
                            apiErrorMessage = errorBody // Fallback
                        }
                    } catch (e: JsonSyntaxException) {
                        Log.e("MerchantRepository", "Error parsing get profile error JSON: $e")
                        apiErrorMessage = errorBody ?: "Unknown error structure."
                    }
                }
                // Specific handling for 401 could be added here if desired
                if (response.code() == 401) {
                    apiErrorMessage = "Unauthorized. Please login again."
                    // Optionally, you could trigger a global logout event here or clear local auth data
                    // AuthManager.logout(context.applicationContext) // Example: clear auth data
                }
                Result.Error(apiErrorMessage)
            }
        } catch (e: Exception) {
            Log.e("MerchantRepository", "Exception during get merchant profile: ${e.message}", e)
            Result.Error("Network error or other exception fetching profile: ${e.message ?: "Unknown error"}", e)
        }
    }
    // --- END: getMerchantProfile function ---
}