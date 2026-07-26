package pl.bochynski.kosmetyki.ui.produkt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun ProduktFormRoute(
    produktId: Long?,
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ProduktFormViewModel = viewModel(
        factory = ProduktFormViewModelFactory(produktId, kategoriaRepository, produktRepository)
    )
    val stan by viewModel.stan.collectAsState()

    LaunchedEffect(stan.zapisano) {
        if (stan.zapisano) naWstecz()
    }

    ProduktFormScreen(
        stan = stan,
        naWstecz = naWstecz,
        naZmianeKategorii = viewModel::ustawKategorie,
        naZmianeMarki = viewModel::ustawMarke,
        naZmianeSerii = viewModel::ustawSerie,
        naZmianeLinii = viewModel::ustawLinie,
        naZmianeNazwy = viewModel::ustawNazwe,
        naWybierzPodpowiedzNazwy = viewModel::wybierzPodpowiedzNazwy,
        naWybierzKandydataUzupelnienia = viewModel::uzupelnijZKandydata,
        naOdrzucUzupelnienie = viewModel::odrzucUzupelnienieDanych,
        naWybierzZUlubionych = viewModel::wybierzZUlubionych,
        naZmianeEan = viewModel::ustawEan,
        naZmianePojemnosci = viewModel::ustawPojemnosc,
        naZmianeDatyWaznosci = viewModel::ustawDateWaznosci,
        naZmianeOkresuZuzycia = viewModel::ustawOkresZuzycia,
        naZmianeJednostki = viewModel::ustawJednostkeOkresu,
        naZmianeDatyZakupu = viewModel::ustawDateZakupu,
        naZmianeCenyZakupu = viewModel::ustawCeneZakupu,
        naZmianeMiejscaZakupu = viewModel::ustawMiejsceZakupu,
        naZmianeNotatki = viewModel::ustawNotatke,
        naZmianeLiczbySztuk = viewModel::ustawLiczbeSztuk,
        naZapisz = viewModel::zapisz,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduktFormScreen(
    stan: ProduktFormUiState,
    naWstecz: () -> Unit,
    naZmianeKategorii: (Long) -> Unit,
    naZmianeMarki: (String) -> Unit,
    naZmianeSerii: (String) -> Unit,
    naZmianeLinii: (String) -> Unit,
    naZmianeNazwy: (String) -> Unit,
    naWybierzPodpowiedzNazwy: (String) -> Unit,
    naWybierzKandydataUzupelnienia: (ProduktEntity) -> Unit,
    naOdrzucUzupelnienie: () -> Unit,
    naWybierzZUlubionych: (ProduktEntity) -> Unit,
    naZmianeEan: (String) -> Unit,
    naZmianePojemnosci: (String) -> Unit,
    naZmianeDatyWaznosci: (LocalDate?) -> Unit,
    naZmianeOkresuZuzycia: (String) -> Unit,
    naZmianeJednostki: (JednostkaOkresuZuzycia) -> Unit,
    naZmianeDatyZakupu: (LocalDate?) -> Unit,
    naZmianeCenyZakupu: (String) -> Unit,
    naZmianeMiejscaZakupu: (String) -> Unit,
    naZmianeNotatki: (String) -> Unit,
    naZmianeLiczbySztuk: (String) -> Unit,
    naZapisz: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pokazDialogUlubionych by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (stan.trybEdycji) "Edytuj produkt" else "Dodaj produkt") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    IconButton(onClick = naZapisz) {
                        Icon(Icons.Filled.Check, contentDescription = "Zapisz")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (stan.trwaLadowanie) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!stan.trybEdycji && stan.ulubione.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { pokazDialogUlubionych = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null)
                        Text(" Wybierz z ulubionych")
                    }
                }

                PoleKategorii(stan = stan, naZmianeKategorii = naZmianeKategorii)

                PoleZAutouzupelnianiem(
                    etykieta = "Marka *",
                    wartosc = stan.marka,
                    podpowiedzi = stan.podpowiedziMarek,
                    naZmiane = naZmianeMarki
                )
                OutlinedTextField(
                    value = stan.seria,
                    onValueChange = naZmianeSerii,
                    label = { Text("Seria") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stan.linia,
                    onValueChange = naZmianeLinii,
                    label = { Text("Linia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                PoleZAutouzupelnianiem(
                    etykieta = "Nazwa *",
                    wartosc = stan.nazwa,
                    podpowiedzi = stan.podpowiedziNazw,
                    naZmiane = naZmianeNazwy,
                    naWybierzPodpowiedz = naWybierzPodpowiedzNazwy
                )
                OutlinedTextField(
                    value = stan.ean,
                    onValueChange = naZmianeEan,
                    label = { Text("Kod EAN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = stan.pojemnosc,
                    onValueChange = naZmianePojemnosci,
                    label = { Text("Pojemność") },
                    placeholder = { Text("np. 50 ml") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                PoleDaty(etykieta = "Data ważności", data = stan.dataWaznosci, naZmiane = naZmianeDatyWaznosci)

                OutlinedTextField(
                    value = stan.okresZuzycia,
                    onValueChange = naZmianeOkresuZuzycia,
                    label = { Text("Okres zużycia po otwarciu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = stan.jednostkaOkresuZuzycia == JednostkaOkresuZuzycia.MIESIACE,
                        onClick = { naZmianeJednostki(JednostkaOkresuZuzycia.MIESIACE) },
                        label = { Text("Miesiące") }
                    )
                    FilterChip(
                        selected = stan.jednostkaOkresuZuzycia == JednostkaOkresuZuzycia.DNI,
                        onClick = { naZmianeJednostki(JednostkaOkresuZuzycia.DNI) },
                        label = { Text("Dni") }
                    )
                }

                PoleDaty(etykieta = "Data zakupu", data = stan.dataZakupu, naZmiane = naZmianeDatyZakupu)

                OutlinedTextField(
                    value = stan.cenaZakupu,
                    onValueChange = naZmianeCenyZakupu,
                    label = { Text("Cena zakupu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                PoleZAutouzupelnianiem(
                    etykieta = "Miejsce zakupu",
                    placeholder = "np. Rossmann",
                    wartosc = stan.miejsceZakupu,
                    podpowiedzi = stan.podpowiedziMiejsc,
                    naZmiane = naZmianeMiejscaZakupu
                )

                OutlinedTextField(
                    value = stan.notatka,
                    onValueChange = naZmianeNotatki,
                    label = { Text("Notatka") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                if (!stan.trybEdycji) {
                    OutlinedTextField(
                        value = stan.liczbaSztuk,
                        onValueChange = naZmianeLiczbySztuk,
                        label = { Text("Liczba sztuk") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                stan.blad?.let { blad ->
                    Text(blad, color = MaterialTheme.colorScheme.error)
                }

                Button(onClick = naZapisz, modifier = Modifier.fillMaxWidth()) {
                    Text(if (stan.trybEdycji) "Zapisz zmiany" else "Dodaj produkt")
                }
            }
        }
    }

    when (stan.kandydaciUzupelnienia.size) {
        0 -> Unit
        1 -> {
            val kandydat = stan.kandydaciUzupelnienia.first()
            AlertDialog(
                onDismissRequest = naOdrzucUzupelnienie,
                title = { Text("Uzupełnić dane?") },
                text = {
                    Text(
                        "Znaleziono w bazie produkt \"${kandydat.marka} ${kandydat.nazwa}\". " +
                            "Czy uzupełnić pozostałe pola (kategoria, marka, seria, linia, EAN, " +
                            "pojemność, okres zużycia) na jego podstawie?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { naWybierzKandydataUzupelnienia(kandydat) }) { Text("Tak") }
                },
                dismissButton = {
                    TextButton(onClick = naOdrzucUzupelnienie) { Text("Nie") }
                }
            )
        }
        else -> {
            AlertDialog(
                onDismissRequest = naOdrzucUzupelnienie,
                title = { Text("Który produkt?") },
                text = {
                    Column {
                        Text("Znaleziono kilka produktów o tej nazwie. Wybierz, na podstawie którego uzupełnić dane:")
                        stan.kandydaciUzupelnienia.forEach { kandydat ->
                            KandydatUzupelnienia(
                                kandydat = kandydat,
                                nazwaKategorii = stan.kategorie.firstOrNull { it.id == kandydat.kategoriaId }?.nazwa,
                                naKlikniecie = { naWybierzKandydataUzupelnienia(kandydat) }
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = naOdrzucUzupelnienie) { Text("Zamknij") }
                }
            )
        }
    }

    if (pokazDialogUlubionych) {
        AlertDialog(
            onDismissRequest = { pokazDialogUlubionych = false },
            title = { Text("Wybierz z ulubionych") },
            text = {
                Column {
                    stan.ulubione.forEach { produkt ->
                        KandydatUzupelnienia(
                            kandydat = produkt,
                            nazwaKategorii = stan.kategorie.firstOrNull { it.id == produkt.kategoriaId }?.nazwa,
                            naKlikniecie = {
                                naWybierzZUlubionych(produkt)
                                pokazDialogUlubionych = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pokazDialogUlubionych = false }) { Text("Zamknij") }
            }
        )
    }
}

@Composable
private fun KandydatUzupelnienia(
    kandydat: ProduktEntity,
    nazwaKategorii: String?,
    naKlikniecie: () -> Unit
) {
    val naglowek = listOfNotNull(
        kandydat.marka.takeIf { it.isNotBlank() },
        kandydat.seria?.takeIf { it.isNotBlank() },
        kandydat.linia?.takeIf { it.isNotBlank() }
    ).joinToString(" ")
    val tytul = if (naglowek.isBlank()) kandydat.nazwa else "$naglowek – ${kandydat.nazwa}"
    val szczegoly = listOfNotNull(
        nazwaKategorii,
        kandydat.pojemnosc?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = naKlikniecie)
            .padding(vertical = 10.dp)
    ) {
        Text(tytul, style = MaterialTheme.typography.bodyLarge)
        if (szczegoly.isNotBlank()) {
            Text(
                szczegoly,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoleKategorii(
    stan: ProduktFormUiState,
    naZmianeKategorii: (Long) -> Unit
) {
    var rozwiniete by remember { mutableStateOf(false) }
    val wybrana = stan.kategorie.firstOrNull { it.id == stan.kategoriaId }

    ExposedDropdownMenuBox(
        expanded = rozwiniete,
        onExpandedChange = { rozwiniete = it }
    ) {
        OutlinedTextField(
            value = wybrana?.nazwa ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategoria *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rozwiniete) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = rozwiniete,
            onDismissRequest = { rozwiniete = false }
        ) {
            stan.kategorie.forEach { kategoria ->
                DropdownMenuItem(
                    text = { Text(kategoria.nazwa) },
                    onClick = {
                        naZmianeKategorii(kategoria.id)
                        rozwiniete = false
                    }
                )
            }
        }
    }
}

private val FORMAT_DATY = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoleDaty(etykieta: String, data: LocalDate?, naZmiane: (LocalDate?) -> Unit) {
    var pokazDialog by remember { mutableStateOf(false) }

    Column {
        Text(
            etykieta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { pokazDialog = true }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data?.format(FORMAT_DATY) ?: "Brak",
                modifier = Modifier.weight(1f)
            )
            if (data != null) {
                IconButton(onClick = { naZmiane(null) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Wyczyść datę")
                }
            }
        }
        HorizontalDivider()
    }

    if (pokazDialog) {
        val stanPickera = rememberDatePickerState(
            initialSelectedDateMillis = data?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pokazDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    stanPickera.selectedDateMillis?.let { millis ->
                        naZmiane(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    pokazDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pokazDialog = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = stanPickera)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoleZAutouzupelnianiem(
    etykieta: String,
    wartosc: String,
    podpowiedzi: List<String>,
    naZmiane: (String) -> Unit,
    placeholder: String? = null,
    naWybierzPodpowiedz: (String) -> Unit = naZmiane
) {
    var rozwiniete by remember { mutableStateOf(false) }
    val przefiltrowane = podpowiedzi.filter {
        it.contains(wartosc, ignoreCase = true) && !it.equals(wartosc, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = rozwiniete && przefiltrowane.isNotEmpty(),
        onExpandedChange = { rozwiniete = it }
    ) {
        OutlinedTextField(
            value = wartosc,
            onValueChange = {
                naZmiane(it)
                rozwiniete = true
            },
            label = { Text(etykieta) },
            placeholder = placeholder?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            singleLine = true
        )
        if (przefiltrowane.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = rozwiniete,
                onDismissRequest = { rozwiniete = false }
            ) {
                przefiltrowane.forEach { podpowiedz ->
                    DropdownMenuItem(
                        text = { Text(podpowiedz) },
                        onClick = {
                            naWybierzPodpowiedz(podpowiedz)
                            rozwiniete = false
                        }
                    )
                }
            }
        }
    }
}
