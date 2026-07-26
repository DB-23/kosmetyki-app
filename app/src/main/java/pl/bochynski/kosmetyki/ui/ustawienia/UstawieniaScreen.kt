package pl.bochynski.kosmetyki.ui.ustawienia

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.repository.DOMYSLNE_KOLORY_STATUSOW
import pl.bochynski.kosmetyki.data.repository.KoloryStatusow
import pl.bochynski.kosmetyki.data.repository.StatusKolorowy
import pl.bochynski.kosmetyki.data.repository.TrybMotywu
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
        naZmianeTrybuMotywu = viewModel::ustawTrybMotywu,
        naZmianeKoloruStatusu = viewModel::ustawKolorStatusu,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstawieniaScreen(
    stan: UstawieniaUiState,
    naWstecz: () -> Unit,
    naZmianeProguDni: (String) -> Unit,
    naZmianeTrybuMotywu: (TrybMotywu) -> Unit,
    naZmianeKoloruStatusu: (StatusKolorowy, Int) -> Unit,
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

    var edytowanyStatus by remember { mutableStateOf<StatusKolorowy?>(null) }
    var wybranaZakladka by remember { mutableStateOf(0) }

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
        ) {
            SecondaryTabRow(selectedTabIndex = wybranaZakladka) {
                Tab(
                    selected = wybranaZakladka == 0,
                    onClick = { wybranaZakladka = 0 },
                    text = { Text("Ogólne") }
                )
                Tab(
                    selected = wybranaZakladka == 1,
                    onClick = { wybranaZakladka = 1 },
                    text = { Text("Informacje") }
                )
            }

            if (wybranaZakladka == 1) {
                ZakladkaInformacje()
            } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Motyw", style = MaterialTheme.typography.titleMedium)
                    listOf(
                        TrybMotywu.SYSTEMOWY to "Systemowy",
                        TrybMotywu.JASNY to "Jasny",
                        TrybMotywu.CIEMNY to "Ciemny"
                    ).forEach { (tryb, etykieta) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { naZmianeTrybuMotywu(tryb) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = stan.trybMotywu == tryb,
                                onClick = { naZmianeTrybuMotywu(tryb) }
                            )
                            Text(etykieta)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Kolory statusów", style = MaterialTheme.typography.titleMedium)
                    WierszKoloruStatusu(
                        etykieta = "Przeterminowane",
                        kolor = stan.koloryStatusow.przeterminowane,
                        naKlikniecie = { edytowanyStatus = StatusKolorowy.PRZETERMINOWANE }
                    )
                    WierszKoloruStatusu(
                        etykieta = "Termin w ciągu 90 dni",
                        kolor = stan.koloryStatusow.pilne,
                        naKlikniecie = { edytowanyStatus = StatusKolorowy.PILNE }
                    )
                    WierszKoloruStatusu(
                        etykieta = "Termin w ciągu 180 dni",
                        kolor = stan.koloryStatusow.wkrotce,
                        naKlikniecie = { edytowanyStatus = StatusKolorowy.WKROTCE }
                    )
                }
            }
        }
            }
        }
    }

    edytowanyStatus?.let { status ->
        DialogWyboruKoloru(
            tytul = tytulStatusu(status),
            aktualnyKolor = kolorDlaStatusu(stan.koloryStatusow, status),
            domyslnyKolor = kolorDlaStatusu(DOMYSLNE_KOLORY_STATUSOW, status),
            naWybierz = { kolor -> naZmianeKoloruStatusu(status, kolor) },
            onDismiss = { edytowanyStatus = null }
        )
    }
}

private fun tytulStatusu(status: StatusKolorowy): String = when (status) {
    StatusKolorowy.PRZETERMINOWANE -> "Kolor: Przeterminowane"
    StatusKolorowy.PILNE -> "Kolor: Termin w ciągu 90 dni"
    StatusKolorowy.WKROTCE -> "Kolor: Termin w ciągu 180 dni"
}

private fun kolorDlaStatusu(kolory: KoloryStatusow, status: StatusKolorowy): Int = when (status) {
    StatusKolorowy.PRZETERMINOWANE -> kolory.przeterminowane
    StatusKolorowy.PILNE -> kolory.pilne
    StatusKolorowy.WKROTCE -> kolory.wkrotce
}

private val PRESETY_KOLOROW = listOf(
    0xFF7F0000.toInt(), 0xFFB71C1C.toInt(), 0xFFD32F2F.toInt(), 0xFFE64A19.toInt(),
    0xFFF57C00.toInt(), 0xFFFF9800.toInt(), 0xFFF9A825.toInt(), 0xFFFBC02D.toInt(),
    0xFF512DA8.toInt(), 0xFF1976D2.toInt(), 0xFF00796B.toInt(), 0xFF388E3C.toInt()
)

@Composable
private fun WierszKoloruStatusu(etykieta: String, kolor: Int, naKlikniecie: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = naKlikniecie)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etykieta, style = MaterialTheme.typography.bodyLarge)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(kolor))
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
    }
}

@Composable
private fun DialogWyboruKoloru(
    tytul: String,
    aktualnyKolor: Int,
    domyslnyKolor: Int,
    naWybierz: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tytul) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PRESETY_KOLOROW.chunked(4).forEach { wiersz ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        wiersz.forEach { kolor ->
                            KoloroweKolko(
                                kolor = kolor,
                                zaznaczony = kolor == aktualnyKolor,
                                naKlikniecie = {
                                    naWybierz(kolor)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Row {
                TextButton(onClick = {
                    naWybierz(domyslnyKolor)
                    onDismiss()
                }) { Text("Domyślny") }
                TextButton(onClick = onDismiss) { Text("Zamknij") }
            }
        }
    )
}

@Composable
private fun KoloroweKolko(kolor: Int, zaznaczony: Boolean, naKlikniecie: () -> Unit) {
    val kolorCompose = Color(kolor)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(kolorCompose)
            .border(
                width = if (zaznaczony) 3.dp else 1.dp,
                color = if (zaznaczony) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = naKlikniecie),
        contentAlignment = Alignment.Center
    ) {
        if (zaznaczony) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Wybrany",
                tint = if (kolorCompose.luminance() > 0.5f) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun ZakladkaInformacje() {
    val context = LocalContext.current
    val wersja = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "—"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Wersja aplikacji", style = MaterialTheme.typography.titleMedium)
                Text(
                    wersja,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Twórca", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Dariusz Bochyński",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "www.dariusz-bochynski.pl",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            val intencja = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.dariusz-bochynski.pl")
                            )
                            context.startActivity(intencja)
                        }
                )
            }
        }
    }
}

private fun sprawdzUprawnienie(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
}
