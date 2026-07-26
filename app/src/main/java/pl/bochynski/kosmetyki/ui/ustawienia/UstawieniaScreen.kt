package pl.bochynski.kosmetyki.ui.ustawienia

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository

@Composable
fun UstawieniaRoute(
    ustawieniaRepository: UstawieniaRepository,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: UstawieniaViewModel = viewModel(factory = UstawieniaViewModelFactory(ustawieniaRepository))
    val stan by viewModel.stan.collectAsState()

    UstawieniaScreen(
        stan = stan,
        naWstecz = naWstecz,
        naZmianeProguDni = viewModel::ustawProgDni,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstawieniaScreen(
    stan: UstawieniaUiState,
    naWstecz: () -> Unit,
    naZmianeProguDni: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uprawnienieWlaczone by remember { mutableStateOf(sprawdzUprawnienie(context)) }

    val launcherUprawnienia = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { przyznane -> uprawnienieWlaczone = przyznane }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obserwator = LifecycleEventObserver { _, zdarzenie ->
            if (zdarzenie == Lifecycle.Event.ON_RESUME) {
                uprawnienieWlaczone = sprawdzUprawnienie(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obserwator)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obserwator) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stan.progDniTekst,
                    onValueChange = naZmianeProguDni,
                    label = { Text("Próg powiadomień (dni)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                stan.blad?.let { blad ->
                    Text(blad, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Raz dziennie sprawdzamy zapasy. Powiadomienie pojawi się, gdy jakiś produkt " +
                        "jest przeterminowany albo jego termin upływa w ciągu podanej liczby dni.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Powiadomienia systemowe", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (uprawnienieWlaczone) "Włączone" else "Wyłączone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!uprawnienieWlaczone) {
                            Button(onClick = { launcherUprawnienia.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                Text("Włącz powiadomienia")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sprawdzUprawnienie(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
}
