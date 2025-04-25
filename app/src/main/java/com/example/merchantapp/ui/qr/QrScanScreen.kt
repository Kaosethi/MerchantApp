// File: app/src/main/java/com/example/merchantapp/ui/qr/QrScanScreen.kt
package com.example.merchantapp.ui.qr

// --- ADD BACK CameraX imports ---
// --- End CameraX imports ---
// --- ADD BACK AndroidView import ---
// --- End AndroidView import ---
// Keep Accompanist Permissions imports
// Keep other necessary imports
// --- ADD BACK coroutine/suspendCoroutine imports ---
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.merchantapp.R
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import java.text.NumberFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.camera.core.Preview as CameraXPreview

// --- End coroutine imports ---


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QrScanScreen(
    amount: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    val formattedAmount = remember(amount) {
        try {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale("en", "US")).format(amountValue)
        } catch (e: Exception) { "Error" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_qr_scan)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Removed verticalArrangement = Arrangement.Center to let Column flow
        ) {
            Text(
                text = stringResource(R.string.qr_label_transaction_amount),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))

            // --- Use PermissionRequired to handle camera display ---
            PermissionRequired(
                permissionState = cameraPermissionState,
                // Content to show when permission is not granted (needs rationale or request)
                permissionNotGrantedContent = {
                    LaunchedEffect(Unit) { // Request permission when content is shown
                        cameraPermissionState.launchPermissionRequest()
                    }
                    Column(
                        Modifier.weight(1f).fillMaxWidth(), // Take up space like preview would
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(R.string.permission_camera_rationale), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text(stringResource(R.string.action_grant_permission))
                        }
                    }
                },
                // Content to show if permission is permanently denied
                permissionNotAvailableContent = {
                    Column(
                        Modifier.weight(1f).fillMaxWidth(), // Take up space like preview would
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(R.string.permission_camera_denied), textAlign = TextAlign.Center)
                        // TODO: Add button to navigate to app settings
                    }
                }
            ) { // Content to show when permission IS granted
                // Display the camera preview
                CameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Allow preview to take available space
                        .padding(vertical = 16.dp)
                        .background(Color.Black) // Add background for visual confirmation
                )
            }
            // --- End PermissionRequired ---

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.qr_instructions),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// --- ADDED BACK: CameraPreview Composable ---
@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val previewView = remember { PreviewView(context) }

    // Use LaunchedEffect keyed on lifecycleOwner to ensure camera restarts if owner changes
    LaunchedEffect(lifecycleOwner) {
        startCamera(context, lifecycleOwner, previewView)
    }

    AndroidView({ previewView }, modifier = modifier) // Use AndroidView import
}

// --- ADDED BACK: startCamera Function ---
private suspend fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView
) {
    val cameraProvider = suspendCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            continuation.resume(future.get())
        }, ContextCompat.getMainExecutor(context))
    }

    // Use the aliased import for CameraX Preview
    val preview = CameraXPreview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        // Must unbind the use-cases before rebinding them.
        cameraProvider.unbindAll()

        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview
            // TODO: Add ImageAnalysis use case here later for actual scanning
        )
        Log.d("QrScanScreen", "Camera bound successfully")

    } catch (exc: Exception) {
        Log.e("QrScanScreen", "Use case binding failed", exc)
        // TODO: Show error message to user?
    }
}


// --- Previews ---
@OptIn(ExperimentalPermissionsApi::class) // Keep OptIn for preview usage
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun QrScanScreenPreview_PermissionGranted() {
    MerchantAppTheme {
        // Simplified preview showing the expected layout when granted
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Transaction Amount: $55.00")
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.DarkGray), contentAlignment = Alignment.Center) { // Darker background
                Text("Camera Preview Area", color=Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text("Point camera at beneficiary QR code.")
        }
    }
}