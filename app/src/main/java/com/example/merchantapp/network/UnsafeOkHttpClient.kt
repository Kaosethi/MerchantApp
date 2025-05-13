// app/src/main/java/com/example/merchantapp/network/UnsafeOkHttpClient.kt
// OR if using Kotlin convention: app/src/main/kotlin/com/example/merchantapp/network/UnsafeOkHttpClient.kt
package com.example.merchantapp.network // Ensure this matches your package structure

import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier

object UnsafeOkHttpClient {

    // Create an OkHttpClient that trusts all certificates and bypasses hostname verification.
    // WARNING: This is INSECURE and should ONLY be used for development/testing purposes
    // (e.g., with ngrok's free tier). DO NOT USE IN PRODUCTION.
    fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    // No-op: accept all client certificates
                }

                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    // No-op: accept all server certificates
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf() // Return an empty array
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL") // You can also try "TLS"
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an SSL socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)

            // Bypass hostname verification.
            // This is necessary because even if the certificate is trusted,
            // the hostname on the certificate might not match the URL (e.g. *.ngrok-free.app vs a specific subdomain)
            builder.hostnameVerifier(HostnameVerifier { hostname, session -> true }) // All hostnames are considered valid

            return builder
        } catch (e: Exception) {
            // Log the exception or handle it more gracefully if needed
            // For now, rethrow as a RuntimeException to make it clear something went wrong during setup
            throw RuntimeException("Failed to create UnsafeOkHttpClient", e)
        }
    }
}