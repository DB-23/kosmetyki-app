package pl.bochynski.kosmetyki.ui.zapasy

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
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.domain.dniDoKonca
import pl.bochynski.kosmetyki.domain.poziomPilnosci
import pl.bochynski.kosmetyki.domain.terminEfektywny
import pl.bochynski.kosmetyki.ui.common.koloryDlaPoziomu
import java.time.format.DateTimeFormatter

@Composable
fun ZapasyRoute(
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
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
    modifier: Modifier = Modifier
) {
    val rozwiniete = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Zapasy") })

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
                        naOznaczOtwarte = naOznaczOtwarte
                    )
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
    naOznaczOtwarte: (ProduktEntity) -> Unit
) {
    Column {
        WierszKarty(
            produkt = grupa.reprezentant,
            liczbaSztuk = grupa.liczbaSztuk,
            pokazKategorie = pokazKategorie,
            nazwaKategorii = nazwaKategorii,
            naKlikniecie = if (grupa.liczbaSztuk > 1) naToggleRozwiniecia else null,
            naOznaczOtwarte = naOznaczOtwarte
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
                        naKlikniecie = null,
                        naOznaczOtwarte = naOznaczOtwarte,
                        etykietaSztuki = "Sztuka ${indeks + 1}"
                    )
                }
            }
        }
    }
}

@Composable
private fun WierszKarty(
    produkt: ProduktEntity,
    liczbaSztuk: Int,
    pokazKategorie: Boolean,
    nazwaKategorii: String?,
    naKlikniecie: (() -> Unit)?,
    naOznaczOtwarte: (ProduktEntity) -> Unit,
    etykietaSztuki: String? = null
) {
    val otwarte = produkt.status == StatusProduktu.OTWARTE
    val poziom = poziomPilnosci(produkt.dniDoKonca())
    val koloryPilnosci = koloryDlaPoziomu(poziom)
    val kolorTla = if (otwarte) MaterialTheme.colorScheme.surfaceVariant else koloryPilnosci.tlo
    val kolorTekstu = if (otwarte) MaterialTheme.colorScheme.onSurfaceVariant else koloryPilnosci.tekst

    val koloryKarty = CardDefaults.cardColors(containerColor = kolorTla, contentColor = kolorTekstu)
    val tresc: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (pokazKategorie && nazwaKategorii != null) {
                    Text(nazwaKategorii, style = MaterialTheme.typography.labelSmall)
                }
                if (etykietaSztuki != null) {
                    Text(etykietaSztuki, style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    text = budujTytul(produkt),
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (otwarte) TextDecoration.LineThrough else TextDecoration.None
                )
                budujOpisTerminu(produkt)?.let { opis ->
                    Text(opis, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (liczbaSztuk > 1) {
                Badge { Text("×$liczbaSztuk") }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Checkbox(
                        checked = otwarte,
                        enabled = !otwarte,
                        onCheckedChange = { if (!otwarte) naOznaczOtwarte(produkt) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = kolorTekstu,
                            uncheckedColor = kolorTekstu
                        )
                    )
                    Text("Otwarte", style = MaterialTheme.typography.labelSmall, color = kolorTekstu)
                }
            }
        }
    }

    val elewacja = CardDefaults.cardElevation(defaultElevation = 2.dp)
    if (naKlikniecie != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = naKlikniecie,
            colors = koloryKarty,
            elevation = elewacja,
            content = { tresc() }
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = koloryKarty,
            elevation = elewacja,
            content = { tresc() }
        )
    }
}

private fun budujTytul(produkt: ProduktEntity): String {
    val naglowek = listOfNotNull(produkt.marka, produkt.seria, produkt.linia)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    return if (naglowek.isBlank()) produkt.nazwa else "$naglowek – ${produkt.nazwa}"
}

private val FORMAT_DATY = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun budujOpisTerminu(produkt: ProduktEntity): String? {
    val dni = produkt.dniDoKonca() ?: return null
    val dataTekst = produkt.terminEfektywny()?.format(FORMAT_DATY) ?: return null
    return when {
        dni <= 0 -> "Przeterminowany od ${-dni} dni ($dataTekst)"
        else -> "Ważne do $dataTekst (jeszcze $dni dni)"
    }
}
