package pl.bochynski.kosmetyki.ui.skaner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
fun SkanerEanRoute(
    naZeskanowano: (String) -> Unit,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    SkanerEanScreen(naZeskanowano = naZeskanowano, naWstecz = naWstecz, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkanerEanScreen(
    naZeskanowano: (String) -> Unit,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uprawnienieWlaczone by remember { mutableStateOf(sprawdzUprawnienieAparatu(context)) }

    val launcherUprawnienia = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { przyznane -> uprawnienieWlaczone = przyznane }

    LaunchedEffect(Unit) {
        if (!uprawnienieWlaczone) {
            launcherUprawnienia.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Skanuj kod EAN") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uprawnienieWlaczone) {
                PodgladKamery(naZeskanowano = naZeskanowano)
                Text(
                    "Wyceluj aparat w kod kreskowy produktu (EAN-13 lub EAN-8)",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Potrzebny jest dostęp do aparatu, żeby zeskanować kod kreskowy.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { launcherUprawnienia.launch(Manifest.permission.CAMERA) }) {
                        Text("Zezwól na dostęp do aparatu")
                    }
                }
            }
        }
    }
}

private fun sprawdzUprawnienieAparatu(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

@Composable
private fun PodgladKamery(naZeskanowano: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val naZeskanowanoAktualne = rememberUpdatedState(naZeskanowano)
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            var juzZeskanowano = false
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val opcjeSkanera = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
                    .build()
                val skaner = BarcodeScanning.getClient(opcjeSkanera)

                val analiza = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analiza.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    val obrazMedia = imageProxy.image
                    if (juzZeskanowano || obrazMedia == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val obraz = InputImage.fromMediaImage(obrazMedia, imageProxy.imageInfo.rotationDegrees)
                    skaner.process(obraz)
                        .addOnSuccessListener { kody ->
                            val kod = kody.firstOrNull()?.rawValue
                            if (kod != null && !juzZeskanowano) {
                                juzZeskanowano = true
                                naZeskanowanoAktualne.value(kod)
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }

                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analiza
                    )
                } catch (_: Exception) {
                    // Brak dostępnego aparatu lub inny blad wiazania - podglad zostanie pusty.
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )

    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
    }
}
