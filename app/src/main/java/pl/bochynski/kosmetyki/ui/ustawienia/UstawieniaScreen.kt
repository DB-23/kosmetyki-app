package pl.bochynski.kosmetyki.ui.ustawienia

import android.Manifest
import android.content.ContentResolver
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity
import pl.bochynski.kosmetyki.data.repository.DOMYSLNE_KOLORY_STATUSOW
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.KoloryStatusow
import pl.bochynski.kosmetyki.data.repository.KonfiguracjaSerweraBazy
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.data.repository.StatusKolorowy
import pl.bochynski.kosmetyki.data.repository.TrybMotywu
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository
import pl.bochynski.kosmetyki.data.seed.DatabaseSeeder
import java.time.LocalDate

@Composable
fun UstawieniaRoute(
    ustawieniaRepository: UstawieniaRepository,
    produktRepository: ProduktRepository,
    kategoriaRepository: KategoriaRepository,
    databaseSeeder: DatabaseSeeder,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: UstawieniaViewModel = viewModel(
        factory = UstawieniaViewModelFactory(
            ustawieniaRepository, produktRepository, kategoriaRepository, databaseSeeder
        )
    )
    val stan by viewModel.stan.collectAsState()

    UstawieniaScreen(
        stan = stan,
        naWstecz = naWstecz,
        naZmianeProguDni = viewModel::ustawProgDni,
        naZmianeTrybuMotywu = viewModel::ustawTrybMotywu,
        naZmianeKoloruStatusu = viewModel::ustawKolorStatusu,
        naWyzerujBaze = viewModel::wyzerujBaze,
        naEksportujDoPliku = viewModel::eksportujDoPliku,
        naImportujZPliku = viewModel::importujZPliku,
        naWczytajBazeDemo = viewModel::wczytajBazeDemonstracyjna,
        naZamknijKomunikatBazy = viewModel::wyczyscKomunikatBazy,
        naZmianeAdresuSerwera = viewModel::ustawAdresSerwera,
        naZmianePortuSerwera = viewModel::ustawPortSerwera,
        naZmianeNazwyUzytkownikaSerwera = viewModel::ustawNazweUzytkownikaSerwera,
        naZmianeHaslaSerwera = viewModel::ustawHasloSerwera,
        naDodajKategorie = viewModel::dodajKategorie,
        naZmienNazweKategorii = viewModel::zmienNazweKategorii,
        naUsunKategorie = viewModel::usunKategorie,
        naZamknijBladKategorii = viewModel::wyczyscBladKategorii,
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
    naWyzerujBaze: () -> Unit,
    naEksportujDoPliku: (ContentResolver, Uri) -> Unit,
    naImportujZPliku: (ContentResolver, Uri, TrybImportu) -> Unit,
    naWczytajBazeDemo: () -> Unit,
    naZamknijKomunikatBazy: () -> Unit,
    naZmianeAdresuSerwera: (String) -> Unit,
    naZmianePortuSerwera: (String) -> Unit,
    naZmianeNazwyUzytkownikaSerwera: (String) -> Unit,
    naZmianeHaslaSerwera: (String) -> Unit,
    naDodajKategorie: (String) -> Unit,
    naZmienNazweKategorii: (KategoriaEntity, String) -> Unit,
    naUsunKategorie: (KategoriaEntity) -> Unit,
    naZamknijBladKategorii: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uprawnienieWlaczone by remember { mutableStateOf(sprawdzUprawnienie(context)) }
    var pokazDialogResetu by remember { mutableStateOf(false) }
    var oczekiwanyPlikImportu by remember { mutableStateOf<Uri?>(null) }

    val launcherEksportu = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) naEksportujDoPliku(context.contentResolver, uri) }

    val launcherImportu = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) oczekiwanyPlikImportu = uri }

    DisposableEffect(Unit) {
        onDispose { naZamknijKomunikatBazy() }
    }

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

            KartaKategorii(
                kategorie = stan.kategorie,
                blad = stan.bladKategorii,
                naDodaj = naDodajKategorie,
                naZmienNazwe = naZmienNazweKategorii,
                naUsun = naUsunKategorie,
                naZamknijBlad = naZamknijBladKategorii
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Zarządzanie bazą danych", style = MaterialTheme.typography.titleMedium)

                    if (stan.trwaOperacjaBazy) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("Przetwarzanie...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    stan.komunikatBazy?.let { komunikat ->
                        Text(
                            komunikat,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            launcherEksportu.launch("kosmetyki-kopia-zapasowa-${LocalDate.now()}.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !stan.trwaOperacjaBazy
                    ) {
                        Text("Eksportuj do pliku")
                    }
                    OutlinedButton(
                        onClick = { launcherImportu.launch(arrayOf("application/json")) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !stan.trwaOperacjaBazy
                    ) {
                        Text("Importuj z pliku")
                    }
                    Text(
                        "Po wybraniu pliku możesz zdecydować, czy dodać produkty do obecnej bazy, " +
                            "czy nią zastąpić.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = naWczytajBazeDemo,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !stan.trwaOperacjaBazy
                    ) {
                        Text("Wczytaj bazę demonstracyjną")
                    }
                    Text(
                        "Dodaje przykładowe produkty (i domyślne kategorie, jeśli ich brakuje) do obecnej bazy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { pokazDialogResetu = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !stan.trwaOperacjaBazy,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Wyzeruj bazę danych")
                    }
                }
            }

            KartaSerweraBazy(
                konfiguracja = stan.konfiguracjaSerwera,
                naZmianeAdresu = naZmianeAdresuSerwera,
                naZmianePortu = naZmianePortuSerwera,
                naZmianeNazwyUzytkownika = naZmianeNazwyUzytkownikaSerwera,
                naZmianeHasla = naZmianeHaslaSerwera
            )
        }
            }
        }
    }

    if (pokazDialogResetu) {
        AlertDialog(
            onDismissRequest = { pokazDialogResetu = false },
            title = { Text("Wyzerować bazę danych?") },
            text = {
                Text(
                    "Wszystkie produkty zostaną trwale usunięte z aplikacji. Tej operacji nie można cofnąć. " +
                        "Rozważ wcześniejszy eksport danych do pliku."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    naWyzerujBaze()
                    pokazDialogResetu = false
                }) { Text("Wyzeruj") }
            },
            dismissButton = {
                TextButton(onClick = { pokazDialogResetu = false }) { Text("Anuluj") }
            }
        )
    }

    oczekiwanyPlikImportu?.let { uri ->
        AlertDialog(
            onDismissRequest = { oczekiwanyPlikImportu = null },
            title = { Text("Jak zaimportować dane?") },
            text = {
                Text(
                    "\"Dodaj do bazy\" dopisze produkty z pliku do obecnej zawartości. " +
                        "\"Zastąp bazę\" najpierw trwale usunie wszystkie obecne produkty, " +
                        "a dopiero potem wczyta dane z pliku. Tej drugiej operacji nie można cofnąć."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    naImportujZPliku(context.contentResolver, uri, TrybImportu.DODAJ)
                    oczekiwanyPlikImportu = null
                }) { Text("Dodaj do bazy") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            naImportujZPliku(context.contentResolver, uri, TrybImportu.ZASTAP)
                            oczekiwanyPlikImportu = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Zastąp bazę") }
                    TextButton(onClick = { oczekiwanyPlikImportu = null }) { Text("Anuluj") }
                }
            }
        )
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

@Composable
private fun KartaKategorii(
    kategorie: List<KategoriaEntity>,
    blad: String?,
    naDodaj: (String) -> Unit,
    naZmienNazwe: (KategoriaEntity, String) -> Unit,
    naUsun: (KategoriaEntity) -> Unit,
    naZamknijBlad: () -> Unit
) {
    var nowaKategoria by remember { mutableStateOf("") }
    var edytowanaKategoria by remember { mutableStateOf<KategoriaEntity?>(null) }
    var kategoriaDoUsuniecia by remember { mutableStateOf<KategoriaEntity?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Kategorie", style = MaterialTheme.typography.titleMedium)

            Column {
                kategorie.forEach { kategoria ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            kategoria.nazwa,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp)
                        )
                        IconButton(onClick = {
                            naZamknijBlad()
                            edytowanaKategoria = kategoria
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Zmień nazwę kategorii ${kategoria.nazwa}")
                        }
                        IconButton(onClick = {
                            naZamknijBlad()
                            kategoriaDoUsuniecia = kategoria
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Usuń kategorię ${kategoria.nazwa}",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nowaKategoria,
                    onValueChange = {
                        nowaKategoria = it
                        naZamknijBlad()
                    },
                    label = { Text("Nowa kategoria") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        naDodaj(nowaKategoria)
                        nowaKategoria = ""
                    },
                    enabled = nowaKategoria.isNotBlank()
                ) {
                    Text("Dodaj")
                }
            }
            blad?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    edytowanaKategoria?.let { kategoria ->
        DialogZmianyNazwyKategorii(
            kategoria = kategoria,
            naZatwierdz = { nowaNazwa -> naZmienNazwe(kategoria, nowaNazwa) },
            onDismiss = { edytowanaKategoria = null }
        )
    }

    kategoriaDoUsuniecia?.let { kategoria ->
        AlertDialog(
            onDismissRequest = { kategoriaDoUsuniecia = null },
            title = { Text("Usunąć kategorię?") },
            text = {
                Text(
                    "Kategoria „${kategoria.nazwa}” zostanie trwale usunięta. " +
                        "Nie da się jej usunąć, jeśli używa jej choć jeden produkt."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        naUsun(kategoria)
                        kategoriaDoUsuniecia = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { kategoriaDoUsuniecia = null }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
private fun DialogZmianyNazwyKategorii(
    kategoria: KategoriaEntity,
    naZatwierdz: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nazwa by remember { mutableStateOf(kategoria.nazwa) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmień nazwę kategorii") },
        text = {
            OutlinedTextField(
                value = nazwa,
                onValueChange = { nazwa = it },
                label = { Text("Nazwa kategorii") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    naZatwierdz(nazwa)
                    onDismiss()
                },
                enabled = nazwa.isNotBlank()
            ) { Text("Zapisz") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@Composable
private fun KartaSerweraBazy(
    konfiguracja: KonfiguracjaSerweraBazy,
    naZmianeAdresu: (String) -> Unit,
    naZmianePortu: (String) -> Unit,
    naZmianeNazwyUzytkownika: (String) -> Unit,
    naZmianeHasla: (String) -> Unit
) {
    var hasloWidoczne by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Serwer bazy danych", style = MaterialTheme.typography.titleMedium)
            Text(
                "Funkcjonalność w przygotowaniu: połączenie z zewnętrznym serwerem bazy " +
                    "produktów (kody EAN) może pojawić się w kolejnych wersjach aplikacji. " +
                    "Poniższe dane są na razie tylko zapisywane lokalnie i nie są nigdzie wysyłane.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = konfiguracja.adres,
                onValueChange = naZmianeAdresu,
                label = { Text("Adres serwera") },
                placeholder = { Text("np. baza.przyklad.pl") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = konfiguracja.port,
                onValueChange = naZmianePortu,
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = konfiguracja.nazwaUzytkownika,
                onValueChange = naZmianeNazwyUzytkownika,
                label = { Text("Nazwa użytkownika") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = konfiguracja.haslo,
                onValueChange = naZmianeHasla,
                label = { Text("Hasło") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (hasloWidoczne) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { hasloWidoczne = !hasloWidoczne }) {
                        Icon(
                            if (hasloWidoczne) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (hasloWidoczne) "Ukryj hasło" else "Pokaż hasło"
                        )
                    }
                }
            )
        }
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

        Text("Historia zmian", style = MaterialTheme.typography.titleMedium)
        HISTORIA_ZMIAN_APLIKACJI.forEach { wpis ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Wersja ${wpis.wersja} · ${wpis.data}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    wpis.zmiany.forEach { zmiana ->
                        Text(
                            "• $zmiana",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
