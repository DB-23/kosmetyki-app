package pl.bochynski.kosmetyki.ui.archiwum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.ui.common.budujTytul
import java.time.format.DateTimeFormatter

@Composable
fun ArchiwumRoute(
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: ArchiwumViewModel = viewModel(
        factory = ArchiwumViewModelFactory(kategoriaRepository, produktRepository)
    )
    val stan by viewModel.stan.collectAsStateWithLifecycle()
    ArchiwumScreen(
        stan = stan,
        naPrzywroc = viewModel::przywrocDoZapasow,
        naUsunTrwale = viewModel::usunTrwale,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiwumScreen(
    stan: ArchiwumUiState,
    naPrzywroc: (ProduktEntity) -> Unit,
    naUsunTrwale: (ProduktEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var produktDoUsuniecia by remember { mutableStateOf<ProduktEntity?>(null) }

    produktDoUsuniecia?.let { produkt ->
        AlertDialog(
            onDismissRequest = { produktDoUsuniecia = null },
            title = { Text("Usunąć trwale?") },
            text = {
                Text(
                    "Produkt „${budujTytul(produkt)}” zostanie usunięty na stałe " +
                        "i nie będzie można go przywrócić. Czy na pewno?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    naUsunTrwale(produkt)
                    produktDoUsuniecia = null
                }) { Text("Usuń trwale", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { produktDoUsuniecia = null }) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Zużyte") }) }
    ) { innerPadding ->
        if (stan.produkty.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (stan.trwaLadowanie) "Wczytywanie..." else "Brak zużytych produktów",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stan.produkty, key = { it.id }) { produkt ->
                    KartaArchiwum(
                        produkt = produkt,
                        nazwaKategorii = stan.nazwyKategorii[produkt.kategoriaId],
                        naPrzywroc = { naPrzywroc(produkt) },
                        naUsunKliknieto = { produktDoUsuniecia = produkt }
                    )
                }
            }
        }
    }
}

private val FORMAT_DATY = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Composable
private fun KartaArchiwum(
    produkt: ProduktEntity,
    nazwaKategorii: String?,
    naPrzywroc: () -> Unit,
    naUsunKliknieto: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            nazwaKategorii?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            Text(budujTytul(produkt), style = MaterialTheme.typography.titleMedium)
            produkt.dataZuzycia?.let {
                Text(
                    "Zużyto: ${it.format(FORMAT_DATY)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = naPrzywroc) { Text("Przywróć") }
                TextButton(
                    onClick = naUsunKliknieto,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń trwale") }
            }
        }
    }
}
