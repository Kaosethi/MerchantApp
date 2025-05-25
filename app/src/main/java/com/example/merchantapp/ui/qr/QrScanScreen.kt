// QrScanScreen.kt
package com.example.merchantapp.ui.qr // Ensure this package name is correct

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.ui.theme.MerchantAppTheme
import com.example.merchantapp.util.QrCodeAnalyzer
import com.example.merchantapp.viewmodel.QrScanViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import androidx.camera.core.Preview as CameraXPreview

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    viewModel: QrScanViewModel = viewModel(), // Standard way to get ViewModel
    onNavigateBack: () -> Unit
) {
    // QrScanUiState and ValidatedBeneficiary are defined within QrScanViewModel.kt
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val formattedAmount = remember(uiState.amount) {
        try {
            val amountValue = uiState.amount.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale("th", "TH")).format(amountValue)
        } catch (e: Exception) {
            Log.e("QrScanScreen", "Failed to format amount: ${uiState.amount}", e)
            "Error" // Default display for amount formatting error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Beneficiary QR") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp) // Overall horizontal padding
        ) {
            Text(
                text = "Amount: $formattedAmount",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // Spacer formerly above dynamic content, removed simulate buttons
            // Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Content Area (Camera/Permission/Loading)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes remaining vertical space
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    when (cameraPermissionState.status) {
                        PermissionStatus.Granted -> {
                            Log.d("QrScanScreen", "Camera permission granted.")
                            CameraPreview(
                                context = context,
                                lifecycleOwner = lifecycleOwner,
                                onQrCodeScanned = { qrValue -> viewModel.onQrCodeScanned(qrValue) },
                                onError = { errorMsg -> viewModel.setErrorMessage(errorMsg) }
                            )
                        }
                        is PermissionStatus.Denied -> {
                            RequestCameraPermission(
                                permissionState = cameraPermissionState,
                                navigateToSettingsScreen = { context.openAppSettings() }
                            )
                        }
                    }
                }
            }

            // Error message display
            uiState.errorMessage?.let { message ->
                Text(
                    text = "Error: $message",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class) // Keep if RequestCameraPermission uses accompanist
@Composable
fun RequestCameraPermission(
    permissionState: PermissionState,
    navigateToSettingsScreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val textToShow = if (permissionState.status.shouldShowRationale) {
            "Scanning QR codes requires camera access."
        } else {
            "Camera permission required. Please grant it in App Settings."
        }
        Text(text = textToShow, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (permissionState.status.shouldShowRationale) {
                permissionState.launchPermissionRequest()
            } else {
                navigateToSettingsScreen()
            }
        }) {
            Text(if (permissionState.status.shouldShowRationale) "Request Permission" else "Open Settings")
        }
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onQrCodeScanned: (String?) -> Unit, // Callback for when QR is scanned
    onError: (String) -> Unit           // Callback for camera errors
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewUseCase by remember { mutableStateOf<CameraXPreview?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    previewUseCase = CameraXPreview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrResult ->
                                // This qrResult is the raw string from the QR code
                                onQrCodeScanned(qrResult)
                            })
                        }
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    try {
                        cameraProvider.unbindAll() // Unbind previous use cases before rebinding.
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageAnalyzer
                        )
                        Log.d("CameraPreview", "CameraX bound to lifecycle.")
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "CameraX binding failed", e)
                        onError("Failed to initialize camera: ${e.localizedMessage}")
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("CameraPreview", "Disposing CameraPreview, shutting down executor.")
            cameraExecutor.shutdown()
            // Optional: Explicitly unbind camera from lifecycle to release resources sooner.
            // cameraProviderFuture.get()?.unbindAll() // Use with caution.
        }
    }
}

fun Context.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

// --- PREVIEWS ---
@OptIn(ExperimentalMaterial3Api::class) // This OptIn might be redundant if QrScanScreen's content is stable
@Preview(showBackground = true, name = "QR Scan Screen - Default")
@Composable
private fun QrScanScreenPreview() {
    MerchantAppTheme {
        // For AndroidViewModel in Preview, provide a dummy Application and SavedStateHandle.
        val dummyApplicationContext = LocalContext.current.applicationContext
        val dummyApplication = if (dummyApplicationContext is Application) {
            dummyApplicationContext
        } else {
            // Fallback for environments where applicationContext might not be Application directly
            // This is less likely but provides a guard.
            Application() // Basic Application instance for preview
        }
        val dummySavedStateHandle = SavedStateHandle(mapOf("amount" to "150.75"))

        QrScanScreen(
            viewModel = QrScanViewModel(dummyApplication, dummySavedStateHandle),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "QR Scan Screen - Requesting Permission")
@Composable
private fun RequestPermissionPreview() {
    MerchantAppTheme {
        // Simulate the state where permission is denied and rationale should be shown
        // For a direct preview of RequestCameraPermission, you might pass a mock PermissionState
        // For now, this just shows the QrScanScreen which would internally render RequestCameraPermission
        val dummyApplicationContext = LocalContext.current.applicationContext
        val dummyApplication = if (dummyApplicationContext is Application) dummyApplicationContext else Application()
        val dummySavedStateHandle = SavedStateHandle(mapOf("amount" to "100.00"))
        // To truly test RequestCameraPermission appearance, you'd mock Accompanist's PermissionState
        // This preview will likely show the CameraPreview part if run on a device with permission,
        // or the RequestCameraPermission if permission is denied by default in preview environment.
        QrScanScreen(
            viewModel = QrScanViewModel(dummyApplication, dummySavedStateHandle),
            onNavigateBack = {}
        )
        // To directly preview RequestCameraPermission:
        // RequestCameraPermission(permissionState = /* Mocked PermissionState */, navigateToSettingsScreen = {})
    }
}