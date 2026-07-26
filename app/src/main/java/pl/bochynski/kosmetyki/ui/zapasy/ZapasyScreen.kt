package pl.bochynski.kosmetyki.ui.zapasy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
fun ZapasyRoute(
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    naDodajProdukt: () -> Unit,
    naEdytujProdukt: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ZapasyViewModel = viewModel(
        factory = ZapasyViewModelFactory(kategoriaRepository, produktRepository)
    )
    val stan by viewModel.stan.collectAsStateWithLifecycle()
    ZapasyScreen(
        stan = stan,
        naWybranaKategoria = viewModel::wybierzKategorie,
        naZmianeSzukania = viewModel::ustawSzukajTekst,
        naOznaczOtwarte = viewModel::oznaczOtwarte,
        naCofnijOtwarcie = viewModel::cofnijOtwarcie,
        naOznaczZuzyte = viewModel::oznaczZuzyte,
        naDodajProdukt = naDodajProdukt,
        naEdytujProdukt = { naEdytujProdukt(it.id) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapasyScreen(
    stan: ZapasyUiState,
    naWybranaKategoria: (Long?) -> Unit,
    naZmianeSzukania: (String) -> Unit,
    naOznaczOtwarte: (ProduktEntity) -> Unit,
    naCofnijOtwarcie: (ProduktEntity) -> Unit,
    naOznaczZuzyte: (ProduktEntity) -> Unit,
    naDodajProdukt: () -> Unit,
    naEdytujProdukt: (ProduktEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val rozwiniete = remember { mutableStateMapOf<String, Boolean>() }
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
        topBar = { TopAppBar(title = { Text("Zapasy") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = naDodajProdukt) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj produkt")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = stan.szukajTekst,
                onValueChange = naZmianeSzukania,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Szukaj...") },
                singleLine = true
            )

            PrimaryScrollableTabRow(selectedTabIndex = indeksZakladki(stan)) {
                Tab(
                    selected = stan.wybranaKategoriaId == null,
                    onClick = { naWybranaKategoria(null) },
                    text = { Text("Wszystkie") }
                )
                stan.kategorie.forEach { kategoria ->
                    Tab(
                        selected = stan.wybranaKategoriaId == kategoria.id,
                        onClick = { naWybranaKategoria(kategoria.id) },
                        text = { Text(kategoria.nazwa) }
                    )
                }
            }

            if (stan.grupy.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (stan.trwaLadowanie) "Wczytywanie..." else "Brak produktów",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stan.grupy, key = { it.kluczListy }) { grupa ->
                        KartaGrupy(
                            grupa = grupa,
                            pokazKategorie = stan.wybranaKategoriaId == null,
                            nazwaKategorii = stan.kategorie.firstOrNull { it.id == grupa.reprezentant.kategoriaId }?.nazwa,
                            rozwinieta = rozwiniete[grupa.kluczListy] == true,
                            naToggleRozwiniecia = {
                                rozwiniete[grupa.kluczListy] = !(rozwiniete[grupa.kluczListy] ?: false)
                            },
                            naOznaczOtwarte = naOznaczOtwarte,
                            naProbaCofnieciaOtwarcia = { produktDoPotwierdzenia = it },
                            naOznaczZuzyte = naOznaczZuzyte,
                            naEdytujProdukt = naEdytujProdukt
                        )
                    }
                }
            }
        }
    }
}

private fun indeksZakladki(stan: ZapasyUiState): Int {
    if (stan.wybranaKategoriaId == null) return 0
    val indeks = stan.kategorie.indexOfFirst { it.id == stan.wybranaKategoriaId }
    return if (indeks == -1) 0 else indeks + 1
}

@Composable
private fun KartaGrupy(
    grupa: GrupaProduktow,
    pokazKategorie: Boolean,
    nazwaKategorii: String?,
    rozwinieta: Boolean,
    naToggleRozwiniecia: () -> Unit,
    naOznaczOtwarte: (ProduktEntity) -> Unit,
    naProbaCofnieciaOtwarcia: (ProduktEntity) -> Unit,
    naOznaczZuzyte: (ProduktEntity) -> Unit,
    naEdytujProdukt: (ProduktEntity) -> Unit
) {
    Column {
        WierszKarty(
            produkt = grupa.reprezentant,
            liczbaSztuk = grupa.liczbaSztuk,
            pokazKategorie = pokazKategorie,
            nazwaKategorii = nazwaKategorii,
            naKlikniecie = if (grupa.liczbaSztuk > 1) naToggleRozwiniecia else { { naEdytujProdukt(grupa.reprezentant) } },
            naOznaczOtwarte = naOznaczOtwarte,
            naProbaCofnieciaOtwarcia = naProbaCofnieciaOtwarcia,
            naOznaczZuzyte = naOznaczZuzyte
        )
        if (rozwinieta && grupa.liczbaSztuk > 1) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grupa.produkty.forEachIndexed { indeks, sztuka ->
                    WierszKarty(
                        produkt = sztuka,
                        liczbaSztuk = 1,
                        pokazKategorie = false,
                        nazwaKategorii = null,
                        naKlikniecie = { naEdytujProdukt(sztuka) },
                        naOznaczOtwarte = naOznaczOtwarte,
                        naProbaCofnieciaOtwarcia = naProbaCofnieciaOtwarcia,
                        naOznaczZuzyte = naOznaczZuzyte,
                        etykietaSztuki = "Sztuka ${indeks + 1}"
                    )
                }
            }
        }
    }
}
