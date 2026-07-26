package pl.bochynski.kosmetyki.ui.otwarte

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import pl.bochynski.kosmetyki.ui.common.DialogCofnijOtwarcie
import pl.bochynski.kosmetyki.ui.common.WierszKarty

@Composable
fun OtwarteRoute(
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    naEdytujProdukt: (Long) -> Unit,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: OtwarteViewModel = viewModel(
        factory = OtwarteViewModelFactory(kategoriaRepository, produktRepository)
    )
    val stan by viewModel.stan.collectAsStateWithLifecycle()
    OtwarteScreen(
        stan = stan,
        naCofnijOtwarcie = viewModel::cofnijOtwarcie,
        naOznaczZuzyte = viewModel::oznaczZuzyte,
        naEdytujProdukt = { naEdytujProdukt(it.id) },
        naWstecz = naWstecz,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtwarteScreen(
    stan: OtwarteUiState,
    naCofnijOtwarcie: (ProduktEntity) -> Unit,
    naOznaczZuzyte: (ProduktEntity) -> Unit,
    naEdytujProdukt: (ProduktEntity) -> Unit,
    naWstecz: () -> Unit,
    modifier: Modifier = Modifier
) {
    var produktDoPotwierdzenia by remember { mutableStateOf<ProduktEntity?>(null) }

    produktDoPotwierdzenia?.let { produkt ->
        DialogCofnijOtwarcie(
            produkt = produkt,
            onConfirm = {
                naCofnijOtwarcie(produkt)
                produktDoPotwierdzenia = null
            },
            onDismiss = { produktDoPotwierdzenia = null }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Otwarte") },
                navigationIcon = {
                    IconButton(onClick = naWstecz) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (stan.produkty.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (stan.trwaLadowanie) "Wczytywanie..." else "Brak otwartych produktów",
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
                    WierszKarty(
                        produkt = produkt,
                        liczbaSztuk = 1,
                        pokazKategorie = true,
                        nazwaKategorii = stan.nazwyKategorii[produkt.kategoriaId],
                        naKlikniecie = { naEdytujProdukt(produkt) },
                        naOznaczOtwarte = {},
                        naProbaCofnieciaOtwarcia = { produktDoPotwierdzenia = it },
                        naOznaczZuzyte = naOznaczZuzyte
                    )
                }
            }
        }
    }
}
