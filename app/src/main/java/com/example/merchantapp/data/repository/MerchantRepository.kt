package com.example.merchantapp.data.repository

import android.util.Log
import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.model.LoginResponse
import com.example.merchantapp.model.MerchantProfileResponse
import com.example.merchantapp.model.TransactionApiResponse
import com.example.merchantapp.model.TransactionItem
import com.example.merchantapp.model.toTransactionItem
import com.example.merchantapp.model.MerchantRegistrationRequest // ADDED IMPORT
import com.example.merchantapp.model.MerchantRegistrationResponse // ADDED IMPORT
import com.example.merchantapp.network.ApiService
import com.example.merchantapp.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

// Result sealed class (utility for handling success/error states)
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val errorCode: Int? = null, val errorMessage: String? = null) : Result<Nothing>()
}

class MerchantRepository(
    private val apiService: ApiService = RetrofitInstance.api
) {

    suspend fun getMerchantProfile(): Result<MerchantProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMerchantProfile()
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error fetching profile"
                    Log.e("MerchantRepository", "Error fetching profile: ${response.code()} - $errorBody")
                    Result.Error(Exception("API Error: ${response.code()} $errorBody"), response.code(), errorBody)
                }
            } catch (e: Exception) {
                Log.e("MerchantRepository", "Exception fetching profile: ${e.message}", e)
                Result.Error(e)
            }
        }
    }

    suspend fun getMerchantTransactionHistory(): Result<List<TransactionItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMerchantTransactions()
                if (response.isSuccessful && response.body() != null) {
                    val domainTransactions = response.body()
                        ?.mapNotNull { apiResponse -> apiResponse.toTransactionItem() }
                        ?: emptyList()
                    Result.Success(domainTransactions)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error fetching transactions"
                    Log.e("MerchantRepository", "Error fetching transactions: ${response.code()} - $errorBody")
                    Result.Error(Exception("API Error: ${response.code()} $errorBody"), response.code(), errorBody)
                }
            } catch (e: Exception) {
                Log.e("MerchantRepository", "Exception fetching transactions: ${e.message}", e)
                Result.Error(e)
            }
        }
    }

    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.merchantLogin(loginRequest)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown login error"
                    Log.e("MerchantRepository", "Error logging in: ${response.code()} - $errorBody")
                    Result.Error(Exception("API Error: ${response.code()} $errorBody"), response.code(), errorBody)
                }
            } catch (e: Exception) {
                Log.e("MerchantRepository", "Exception during login: ${e.message}", e)
                Result.Error(e)
            }
        }
    }

    // --- ADDED: Function for Merchant Registration ---
    suspend fun registerMerchant(request: MerchantRegistrationRequest): Result<MerchantRegistrationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("MerchantRepository", "Attempting to register merchant: ${request.email}")
                val response = apiService.merchantRegister(request)
                if (response.isSuccessful && response.body() != null) {
                    Log.i("MerchantRepository", "Merchant registration successful for ${request.email}: ${response.body()?.message}")
                    Result.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown registration error"
                    val errorCode = response.code()
                    Log.e("MerchantRepository", "Merchant registration failed for ${request.email}: $errorCode - $errorBody")
                    Result.Error(Exception("API Error: $errorCode $errorBody"), errorCode, errorBody)
                }
            } catch (e: Exception) {
                Log.e("MerchantRepository", "Exception during merchant registration for ${request.email}: ${e.message}", e)
                Result.Error(e)
            }
        }
    }
    // --- END ADDED ---

    // TODO: Add repository methods for other API calls (beneficiaries, processing transactions, etc.)
}