package pl.bochynski.kosmetyki.ui.historiacen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoriaCenRoute(
    produktRepository: ProduktRepository,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HistoriaCenViewModel = viewModel(factory = HistoriaCenViewModelFactory(produktRepository))
    val stan by viewModel.stan.collectAsState()

    HistoriaCenScreen(
        stan = stan,
        naWstecz = naWstecz,
        naWybierzProdukt = viewModel::wybierzProdukt,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoriaCenScreen(
    stan: HistoriaCenUiState,
    naWstecz: () -> Unit,
    naWybierzProdukt: (KandydatHistorii) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Historia cen") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (stan.trwaLadowanie) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (stan.kandydaci.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Brak danych cenowych. Podaj cenę i datę zakupu w formularzu produktu, " +
                        "żeby zobaczyć tutaj historię cen.",
                    style = MaterialTheme.typography.bodyLarge
                )
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
                WyborProduktu(
                    kandydaci = stan.kandydaci,
                    wybrany = stan.wybrany,
                    naWybierz = naWybierzProdukt
                )

                if (stan.punkty.isEmpty()) {
                    Text("Brak danych cenowych dla tego produktu.")
                } else {
                    val min = stan.punkty.minOf { it.cena }
                    val max = stan.punkty.maxOf { it.cena }
                    Text(
                        if (stan.punkty.size == 1) {
                            "1 zakup"
                        } else {
                            "${odmienZakupy(stan.punkty.size)} • od ${formatujCene(min)} do ${formatujCene(max)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card {
                        WykresCen(
                            punkty = stan.punkty,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        stan.punkty.forEach { punkt ->
                            WierszCeny(punkt)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WyborProduktu(
    kandydaci: List<KandydatHistorii>,
    wybrany: KandydatHistorii?,
    naWybierz: (KandydatHistorii) -> Unit
) {
    var rozwiniete by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = rozwiniete,
        onExpandedChange = { rozwiniete = it }
    ) {
        OutlinedTextField(
            value = wybrany?.etykieta ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Produkt") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rozwiniete) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = rozwiniete,
            onDismissRequest = { rozwiniete = false }
        ) {
            kandydaci.forEach { kandydat ->
                DropdownMenuItem(
                    text = { Text(kandydat.etykieta) },
                    onClick = {
                        naWybierz(kandydat)
                        rozwiniete = false
                    }
                )
            }
        }
    }
}

private val FORMAT_DATY = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val LOCALE_PL: Locale = Locale.Builder().setLanguage("pl").setRegion("PL").build()

private fun formatujCene(wartosc: Double): String = String.format(LOCALE_PL, "%.2f zł", wartosc)

private fun odmienZakupy(liczba: Int): String {
    val ostatniaCyfra = liczba % 10
    val ostatnieDwieCyfry = liczba % 100
    val forma = if (ostatnieDwieCyfry in 12..14) {
        "zakupów"
    } else when (ostatniaCyfra) {
        2, 3, 4 -> "zakupy"
        else -> "zakupów"
    }
    return "$liczba $forma"
}

@Composable
private fun WierszCeny(punkt: PunktCeny) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(punkt.data.format(FORMAT_DATY), style = MaterialTheme.typography.bodyMedium)
        Text(formatujCene(punkt.cena), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun WykresCen(punkty: List<PunktCeny>, modifier: Modifier = Modifier) {
    val kolorLinii = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val minCena = punkty.minOf { it.cena }
        val maxCena = punkty.maxOf { it.cena }
        val zakresCeny = (maxCena - minCena).takeIf { it > 0.0 } ?: 1.0
        val minDzien = punkty.first().data.toEpochDay()
        val maxDzien = punkty.last().data.toEpochDay()
        val zakresDni = (maxDzien - minDzien).takeIf { it > 0L } ?: 1L

        val paddingPx = 8.dp.toPx()
        val szerokosc = size.width - 2 * paddingPx
        val wysokosc = size.height - 2 * paddingPx

        fun offsetDlaPunktu(punkt: PunktCeny): Offset {
            val postepX = (punkt.data.toEpochDay() - minDzien).toFloat() / zakresDni
            val x = paddingPx + (if (punkty.size == 1) szerokosc / 2 else postepX * szerokosc)
            val postepY = ((punkt.cena - minCena) / zakresCeny).toFloat()
            val y = paddingPx + (1f - postepY) * wysokosc
            return Offset(x, y)
        }

        if (punkty.size == 1) {
            drawCircle(color = kolorLinii, radius = 6.dp.toPx(), center = offsetDlaPunktu(punkty.first()))
        } else {
            val sciezka = Path()
            punkty.forEachIndexed { indeks, punkt ->
                val offset = offsetDlaPunktu(punkt)
                if (indeks == 0) sciezka.moveTo(offset.x, offset.y) else sciezka.lineTo(offset.x, offset.y)
            }
            drawPath(sciezka, color = kolorLinii, style = Stroke(width = 3.dp.toPx()))
            punkty.forEach { punkt ->
                drawCircle(color = kolorLinii, radius = 5.dp.toPx(), center = offsetDlaPunktu(punkt))
            }
        }
    }
}
