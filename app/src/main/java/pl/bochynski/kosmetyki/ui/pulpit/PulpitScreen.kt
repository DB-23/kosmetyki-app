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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import java.util.Locale

@Composable
fun PulpitRoute(
    produktRepository: ProduktRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: PulpitViewModel = viewModel(factory = PulpitViewModelFactory(produktRepository))
    val stan by viewModel.stan.collectAsState()

    PulpitScreen(stan = stan, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulpitScreen(stan: PulpitUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Pulpit") }) }
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
                NeutralnaKarta(tytul = "Wszystkie kosmetyki", wartosc = stan.liczbaWszystkich.toString())

                LicznikKarta(
                    tytul = "Przeterminowane",
                    liczba = stan.liczbaPrzeterminowanych,
                    poziom = PoziomPilnosci.PRZETERMINOWANY
                )
                LicznikKarta(
                    tytul = "Termin w ciągu 90 dni",
                    liczba = stan.liczbaPilnych,
                    poziom = PoziomPilnosci.PILNY
                )
                LicznikKarta(
                    tytul = "Termin w ciągu 180 dni",
                    liczba = stan.liczbaWkrotce,
                    poziom = PoziomPilnosci.WKROTCE
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
                        modifier = Modifier.weight(1f)
                    )
                    NeutralnaKarta(
                        tytul = "Otwarte",
                        wartosc = stan.liczbaOtwartych.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                NeutralnaKarta(tytul = "Wartość zapasów", wartosc = formatujCene(stan.wartoscZapasow))
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
            }
        }
    }
}

private val LOCALE_PL: Locale = Locale.Builder().setLanguage("pl").setRegion("PL").build()

private fun formatujCene(wartosc: Double): String =
    String.format(LOCALE_PL, "%.2f zł", wartosc)

@Composable
private fun LicznikKarta(tytul: String, liczba: Int, poziom: PoziomPilnosci) {
    val kolory = koloryDlaPoziomu(poziom)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = kolory.tlo, contentColor = kolory.tekst)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(tytul, style = MaterialTheme.typography.titleMedium)
            Text(liczba.toString(), style = MaterialTheme.typography.displaySmall)
        }
    }
}

@Composable
private fun NeutralnaKarta(
    tytul: String,
    wartosc: String,
    modifier: Modifier = Modifier,
    styl: TextStyle = MaterialTheme.typography.displaySmall
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(tytul, style = MaterialTheme.typography.titleMedium)
            Text(wartosc, style = styl)
        }
    }
}
