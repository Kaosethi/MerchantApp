package com.example.merchantapp.network // Adjust if your root package is different

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient // Import OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // Import Interceptor

/**
 * Provides a configured singleton instance of Retrofit and the ApiService.
 */
object RetrofitInstance {

    // !!! IMPORTANT: REPLACE WITH YOUR ACTUAL BACKEND API BASE URL !!!
    // It should end with a '/'
    private const val BASE_URL = "http://10.0.2.2:5001/" // Example: Localhost from Android Emulator

    // --- Optional: Add Logging Interceptor for Debugging ---
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request/response bodies
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Add the interceptor
        .build()
    // --- End Optional Logging ---


    // Lazy initialization of the Retrofit instance.
    // It's created only when first accessed.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // MODIFIED: Use client with interceptor
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
            .build()
    }

    // Lazy initialization of the ApiService implementation.
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

/*
Notes on BASE_URL:
- If your backend API is running on the same machine as your Android Emulator:
    - Use `http://10.0.2.2:<PORT>/` (10.0.2.2 is a special alias for the emulator's host loopback interface). Replace <PORT> with the actual port your backend runs on (e.g., 5001, 8080, 3000).
- If your backend is deployed somewhere (e.g., cloud server, different machine on the same network):
    - Use its actual IP address or domain name (e.g., `http://192.168.1.100:5001/`, `https://api.yourdomain.com/`).
- Ensure your backend allows connections from the emulator/device.
- If using HTTPS, ensure the URL starts with `https://`.
- **The URL MUST end with a forward slash `/`**
*/

/*
Notes on Network Security Config (If using HTTP instead of HTTPS for local development):
- If your BASE_URL uses `http://` (not `https://`), Android blocks these cleartext connections by default on recent versions.
- You might need to create a network security configuration file to allow cleartext traffic specifically for your local development domain (like 10.0.2.2).
- Create `app/src/main/res/xml/network_security_config.xml`:
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <network-security-config>
        <domain-config cleartextTrafficPermitted="true">
            <domain includeSubdomains="true">10.0.2.2</domain> <!-- Or your local IP -->
        </domain-config>
    </network-security-config>
    ```
- Reference this file in your `AndroidManifest.xml` inside the `<application>` tag:
    ```xml
    <application
        ...
        android:networkSecurityConfig="@xml/network_security_config"
        ...>
    ```
- Only do this for local development! Production APIs should use HTTPS.
*/