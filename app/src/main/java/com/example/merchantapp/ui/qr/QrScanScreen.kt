// QrScanScreen.kt
package com.example.merchantapp.ui.qr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraXPreview // Alias remains
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.merchantapp.R
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

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    viewModel: QrScanViewModel = viewModel(),
    onNavigateBack: () -> Unit
    // REMOVED onQrScanSuccessNavigation parameter - OK
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // REMOVED: LaunchedEffect for navigation, as it's handled in MainActivity observing the ViewModel
    // LaunchedEffect(uiState.isQrCodeDetected, uiState.scannedQrCodeValue) {
    //     if (uiState.isQrCodeDetected && uiState.scannedQrCodeValue != null) {
    //         // Navigation logic moved to MainActivity's composable block
    //         viewModel.onNavigationHandled()
    //     }
    // }

    val formattedAmount = remember(uiState.amount) {
        try {
            val amountValue = uiState.amount.toDoubleOrNull() ?: 0.0
            NumberFormat.getCurrencyInstance(Locale("th", "TH")).format(amountValue)
        } catch (e: Exception) {
            Log.e("QrScanScreen", "Failed to format amount: ${uiState.amount}", e)
            "Error"
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
        ) {
            Text(
                text = "Amount: $formattedAmount",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )

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

            uiState.errorMessage?.let { message ->
                Text(
                    text = "Error: $message",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- RequestCameraPermission, CameraPreview, openAppSettings, Previews remain the same ---
@OptIn(ExperimentalPermissionsApi::class)
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
            "Camera permission required for this feature. Please grant the permission in App Settings."
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
    onQrCodeScanned: (String?) -> Unit,
    onError: (String) -> Unit
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
                                onQrCodeScanned(qrResult)
                            })
                        }
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    try {
                        cameraProvider.unbindAll()
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
        }
    }
}

fun Context.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun QrScanScreenPreview() {
    MerchantAppTheme {
        Scaffold(topBar = {TopAppBar(title = {Text("Scan Beneficiary QR")})}) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center){
                Text("Camera Preview Area (Requires Device/Emulator)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RequestPermissionPreview() {
    MerchantAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Camera permission required...", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {}) { Text("Request Permission") }
        }
    }
}