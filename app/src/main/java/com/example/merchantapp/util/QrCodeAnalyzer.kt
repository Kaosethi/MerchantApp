// QrCodeAnalyzer.kt
package com.example.merchantapp.util // Or your preferred package

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

// ADDED: Entire new file

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String?) -> Unit // Callback to pass the result
) : ImageAnalysis.Analyzer {

    // Configure scanner for QR codes only
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError") // Needed for image.mediaImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        // Get the first detected QR code's raw value
                        val qrCodeValue = barcodes[0].rawValue
                        Log.d("QrCodeAnalyzer", "QR Code found: $qrCodeValue")
                        onQrCodeScanned(qrCodeValue) // Call the callback
                    } else {
                        Log.v("QrCodeAnalyzer", "No QR Code found in this frame.")
                        // Optional: Could call onQrCodeScanned(null) if you need to know when nothing is found
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrCodeAnalyzer", "Barcode scanning failed", e)
                    // Handle failure, maybe call callback with null or an error indicator
                    onQrCodeScanned(null) // Indicate failure or no result
                }
                .addOnCompleteListener {
                    // IMPORTANT: Close the imageProxy to allow the next frame to be processed.
                    // Failure to close it will halt the camera feed.
                    imageProxy.close()
                }
        } else {
            // If mediaImage is null, close the proxy anyway.
            Log.w("QrCodeAnalyzer", "MediaImage was null, closing proxy.")
            imageProxy.close()
        }
    }
}