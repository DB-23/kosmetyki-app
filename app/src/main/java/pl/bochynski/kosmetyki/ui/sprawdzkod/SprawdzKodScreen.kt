package pl.bochynski.kosmetyki.ui.sprawdzkod

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.ui.common.WykresCen
import java.util.Locale

@Composable
fun SprawdzKodRoute(
    ean: String,
    produktRepository: ProduktRepository,
    naWstecz: () -> Unit,
    naDodajProdukt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SprawdzKodViewModel = viewModel(
        factory = SprawdzKodViewModelFactory(ean, produktRepository)
    )
    val stan by viewModel.stan.collectAsState()

    SprawdzKodScreen(
        stan = stan,
        naWstecz = naWstecz,
        naDodajProdukt = { naDodajProdukt(stan.ean) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprawdzKodScreen(
    stan: SprawdzKodUiState,
    naWstecz: () -> Unit,
    naDodajProdukt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Sprawdź kod kreskowy") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            stan.trwaLadowanie -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            !stan.znaleziono -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Nie znaleziono danych dla kodu ${stan.ean} ani w bazie aplikacji, " +
                                "ani w zewnętrznej bazie Open Beauty Facts.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = naDodajProdukt) {
                            Text("Dodaj ręcznie ten kosmetyk")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NaglowekProduktu(stan)

                    if (stan.zrodloZewnetrzne) {
                        Text(
                            "Ten kosmetyk nie znajduje się jeszcze w Twojej bazie. " +
                                "Dane pochodzą z zewnętrznej bazy Open Beauty Facts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = naDodajProdukt) {
                            Text("Dodaj do bazy")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatystykaKarta(
                                tytul = "Na stanie",
                                wartosc = (stan.liczbaWZapasie + stan.liczbaOtwartych).toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatystykaKarta(
                                tytul = "Łącznie kupione",
                                wartosc = stan.liczbaLacznie.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatystykaKarta(
                                tytul = "W zapasie",
                                wartosc = stan.liczbaWZapasie.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatystykaKarta(
                                tytul = "Otwarte",
                                wartosc = stan.liczbaOtwartych.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatystykaKarta(
                                tytul = "Zużyte",
                                wartosc = stan.liczbaZuzytych.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        StatystykaKarta(
                            tytul = "Średnia cena",
                            wartosc = stan.sredniaCena?.let { formatujCeneSprawdzKod(it) } ?: "Brak danych"
                        )
                        StatystykaKarta(
                            tytul = "Najczęstsze miejsce zakupu",
                            wartosc = stan.najczestszeMiejsce ?: "Brak danych"
                        )

                        if (stan.punkty.isEmpty()) {
                            Text("Brak danych cenowych dla tego produktu.")
                        } else {
                            Text(
                                "Historia cen",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Card {
                                WykresCen(
                                    punkty = stan.punkty,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Button(onClick = naDodajProdukt) {
                            Text("Dodaj kolejny zakup")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NaglowekProduktu(stan: SprawdzKodUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                listOfNotNull(stan.marka, stan.nazwa).joinToString(" ").ifBlank { "Nieznany produkt" },
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Kod EAN: ${stan.ean}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StatystykaKarta(tytul: String, wartosc: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(tytul, style = MaterialTheme.typography.titleSmall)
            Text(wartosc, style = MaterialTheme.typography.titleLarge)
        }
    }
}

private val LOCALE_PL_SPRAWDZ_KOD: Locale = Locale.Builder().setLanguage("pl").setRegion("PL").build()

private fun formatujCeneSprawdzKod(wartosc: Double): String =
    String.format(LOCALE_PL_SPRAWDZ_KOD, "%.2f zł", wartosc)
