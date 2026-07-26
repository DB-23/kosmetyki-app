package pl.bochynski.kosmetyki.ui.produktdetal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.ui.common.budujTytul
import java.time.format.DateTimeFormatter

@Composable
fun ProduktDetalRoute(
    produktId: Long,
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    naWstecz: () -> Unit,
    naEdytuj: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ProduktDetalViewModel = viewModel(
        factory = ProduktDetalViewModelFactory(produktId, kategoriaRepository, produktRepository)
    )
    val stan by viewModel.stan.collectAsState()

    ProduktDetalScreen(
        stan = stan,
        naWstecz = naWstecz,
        naEdytuj = naEdytuj,
        naPrzelaczUlubiony = viewModel::przelaczUlubiony,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduktDetalScreen(
    stan: ProduktDetalUiState,
    naWstecz: () -> Unit,
    naEdytuj: () -> Unit,
    naPrzelaczUlubiony: () -> Unit,
    modifier: Modifier = Modifier
) {
    val produkt = stan.produkt

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(produkt?.let { budujTytul(it) } ?: "Szczegóły produktu") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    if (produkt != null) {
                        IconButton(onClick = naPrzelaczUlubiony) {
                            Icon(
                                imageVector = if (produkt.ulubiony) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (produkt.ulubiony) "Usuń z ulubionych" else "Dodaj do ulubionych"
                            )
                        }
                        IconButton(onClick = naEdytuj) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edytuj")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (stan.trwaLadowanie || produkt == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (stan.trwaLadowanie) CircularProgressIndicator() else Text("Nie znaleziono produktu")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PolePodgladu("Kategoria", stan.nazwaKategorii)
                PolePodgladu("Marka", produkt.marka)
                PolePodgladu("Seria", produkt.seria)
                PolePodgladu("Linia", produkt.linia)
                PolePodgladu("Nazwa", produkt.nazwa)
                PolePodgladu("Kod EAN", produkt.ean)
                PolePodgladu("Pojemność", produkt.pojemnosc)
                PolePodgladu("Data ważności", produkt.dataWaznosci?.format(FORMAT_DATY))
                PolePodgladu("Okres zużycia po otwarciu", opisOkresuZuzycia(produkt))
                PolePodgladu("Status", opisStatusu(produkt.status))
                PolePodgladu("Data otwarcia", produkt.dataOtwarcia?.format(FORMAT_DATY))
                PolePodgladu("Data zużycia", produkt.dataZuzycia?.format(FORMAT_DATY))
                PolePodgladu("Data zakupu", produkt.dataZakupu?.format(FORMAT_DATY))
                PolePodgladu("Cena zakupu", produkt.cenaZakupu?.let { "%.2f zł".format(it) })
                PolePodgladu("Miejsce zakupu", produkt.miejsceZakupu)
                PolePodgladu("Notatka", produkt.notatka)
                PolePodgladu("Data dodania", produkt.dataDodania.format(FORMAT_DATY))
            }
        }
    }
}

private val FORMAT_DATY = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun opisOkresuZuzycia(produkt: ProduktEntity): String? {
    val okres = produkt.okresZuzyciaPoOtwarciu ?: return null
    val jednostka = when (produkt.jednostkaOkresuZuzycia) {
        JednostkaOkresuZuzycia.MIESIACE -> "miesięcy"
        JednostkaOkresuZuzycia.DNI -> "dni"
    }
    return "$okres $jednostka"
}

private fun opisStatusu(status: StatusProduktu): String = when (status) {
    StatusProduktu.W_ZAPASIE -> "W zapasie"
    StatusProduktu.OTWARTE -> "Otwarte"
    StatusProduktu.ZUZYTE -> "Zużyte"
}

@Composable
private fun PolePodgladu(etykieta: String, wartosc: String?) {
    if (wartosc.isNullOrBlank()) return
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(etykieta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(wartosc, style = MaterialTheme.typography.bodyLarge)
    }
    HorizontalDivider()
}
