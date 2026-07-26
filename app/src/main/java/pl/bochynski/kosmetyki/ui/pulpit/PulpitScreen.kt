package pl.bochynski.kosmetyki.ui.pulpit

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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.domain.PoziomPilnosci
import pl.bochynski.kosmetyki.ui.common.koloryDlaPoziomu
import pl.bochynski.kosmetyki.ui.filtrowanalista.RodzajFiltra
import java.util.Locale

@Composable
fun PulpitRoute(
    produktRepository: ProduktRepository,
    naHistorieCen: () -> Unit,
    naUstawienia: () -> Unit,
    naListaFiltrowana: (RodzajFiltra) -> Unit,
    naOtwarte: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PulpitViewModel = viewModel(factory = PulpitViewModelFactory(produktRepository))
    val stan by viewModel.stan.collectAsState()

    PulpitScreen(
        stan = stan,
        naHistorieCen = naHistorieCen,
        naUstawienia = naUstawienia,
        naListaFiltrowana = naListaFiltrowana,
        naOtwarte = naOtwarte,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulpitScreen(
    stan: PulpitUiState,
    naHistorieCen: () -> Unit,
    naUstawienia: () -> Unit,
    naListaFiltrowana: (RodzajFiltra) -> Unit,
    naOtwarte: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Pulpit") },
                actions = {
                    IconButton(onClick = naUstawienia) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ustawienia")
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
                NeutralnaKarta(
                    tytul = "Wszystkie kosmetyki",
                    wartosc = stan.liczbaWszystkich.toString(),
                    naKlikniecie = { naListaFiltrowana(RodzajFiltra.WSZYSTKIE) }
                )

                LicznikKarta(
                    tytul = "Przeterminowane",
                    liczba = stan.liczbaPrzeterminowanych,
                    poziom = PoziomPilnosci.PRZETERMINOWANY,
                    naKlikniecie = { naListaFiltrowana(RodzajFiltra.PRZETERMINOWANE) }
                )
                LicznikKarta(
                    tytul = "Termin w ciągu 90 dni",
                    liczba = stan.liczbaPilnych,
                    poziom = PoziomPilnosci.PILNY,
                    naKlikniecie = { naListaFiltrowana(RodzajFiltra.PILNE) }
                )
                LicznikKarta(
                    tytul = "Termin w ciągu 180 dni",
                    liczba = stan.liczbaWkrotce,
                    poziom = PoziomPilnosci.WKROTCE,
                    naKlikniecie = { naListaFiltrowana(RodzajFiltra.WKROTCE) }
                )

                Text(
                    "Statystyki",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeutralnaKarta(
                        tytul = "W zapasie",
                        wartosc = stan.liczbaWZapasie.toString(),
                        modifier = Modifier.weight(1f),
                        naKlikniecie = { naListaFiltrowana(RodzajFiltra.W_ZAPASIE) }
                    )
                    NeutralnaKarta(
                        tytul = "Otwarte",
                        wartosc = stan.liczbaOtwartych.toString(),
                        modifier = Modifier.weight(1f),
                        naKlikniecie = naOtwarte
                    )
                }

                NeutralnaKarta(tytul = "Wartość zapasów", wartosc = formatujCene(stan.wartoscZapasow))
                NeutralnaKarta(
                    tytul = "Średnia cena kosmetyku",
                    wartosc = stan.sredniaCena?.let { formatujCene(it) } ?: "Brak danych"
                )
                NeutralnaKarta(
                    tytul = "Najczęściej wybierana marka",
                    wartosc = stan.najczestszaMarka ?: "Brak danych",
                    styl = MaterialTheme.typography.titleLarge
                )
                NeutralnaKarta(
                    tytul = "Najczęstsze miejsce zakupu",
                    wartosc = stan.najczestszeMiejsceZakupu ?: "Brak danych",
                    styl = MaterialTheme.typography.titleLarge
                )

                KartaHistoriiCen(naKlikniecie = naHistorieCen)
            }
        }
    }
}

private val LOCALE_PL: Locale = Locale.Builder().setLanguage("pl").setRegion("PL").build()

private fun formatujCene(wartosc: Double): String =
    String.format(LOCALE_PL, "%.2f zł", wartosc)

@Composable
private fun LicznikKarta(
    tytul: String,
    liczba: Int,
    poziom: PoziomPilnosci,
    naKlikniecie: (() -> Unit)? = null
) {
    val kolory = koloryDlaPoziomu(poziom)
    val koloryKarty = CardDefaults.cardColors(containerColor = kolory.tlo, contentColor = kolory.tekst)
    val tresc: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(tytul, style = MaterialTheme.typography.titleMedium)
            Text(liczba.toString(), style = MaterialTheme.typography.displaySmall)
        }
    }
    if (naKlikniecie != null) {
        Card(modifier = Modifier.fillMaxWidth(), onClick = naKlikniecie, colors = koloryKarty, content = { tresc() })
    } else {
        Card(modifier = Modifier.fillMaxWidth(), colors = koloryKarty, content = { tresc() })
    }
}

@Composable
private fun KartaHistoriiCen(naKlikniecie: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = naKlikniecie,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null)
            Text("Historia cen", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun NeutralnaKarta(
    tytul: String,
    wartosc: String,
    modifier: Modifier = Modifier,
    styl: TextStyle = MaterialTheme.typography.displaySmall,
    naKlikniecie: (() -> Unit)? = null
) {
    val kolory = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val tresc: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(tytul, style = MaterialTheme.typography.titleMedium)
            Text(wartosc, style = styl)
        }
    }
    if (naKlikniecie != null) {
        Card(modifier = modifier.fillMaxWidth(), onClick = naKlikniecie, colors = kolory, content = { tresc() })
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = kolory, content = { tresc() })
    }
}
