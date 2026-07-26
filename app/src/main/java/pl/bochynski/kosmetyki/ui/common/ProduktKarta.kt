package pl.bochynski.kosmetyki.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.domain.dniDoKonca
import pl.bochynski.kosmetyki.domain.poziomPilnosci
import pl.bochynski.kosmetyki.domain.terminEfektywny
import java.time.format.DateTimeFormatter

/**
 * Karta pojedynczej fizycznej sztuki (lub grupy identycznych nieotwartych sztuk — [liczbaSztuk] > 1).
 * Współdzielona przez ekrany Zapasy i Otwarte.
 */
@Composable
fun WierszKarty(
    produkt: ProduktEntity,
    liczbaSztuk: Int,
    pokazKategorie: Boolean,
    nazwaKategorii: String?,
    naKlikniecie: (() -> Unit)?,
    naOznaczOtwarte: (ProduktEntity) -> Unit,
    naProbaCofnieciaOtwarcia: (ProduktEntity) -> Unit,
    naOznaczZuzyte: (ProduktEntity) -> Unit,
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
                Row(verticalAlignment = Alignment.Top) {
                    if (otwarte) {
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = "Produkt otwarty",
                            tint = kolorTekstu,
                            modifier = Modifier
                                .padding(top = 3.dp, end = 4.dp)
                                .size(16.dp)
                        )
                    }
                    Text(
                        text = budujTytul(produkt),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                budujOpisTerminu(produkt)?.let { opis ->
                    Text(opis, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (liczbaSztuk > 1) {
                Badge { Text("×$liczbaSztuk") }
            } else {
                IconButton(onClick = { naOznaczZuzyte(produkt) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Oznacz jako zużyte",
                        tint = kolorTekstu
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Checkbox(
                        checked = otwarte,
                        enabled = true,
                        onCheckedChange = { zaznaczone ->
                            if (zaznaczone) naOznaczOtwarte(produkt) else naProbaCofnieciaOtwarcia(produkt)
                        },
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

@Composable
fun DialogCofnijOtwarcie(produkt: ProduktEntity, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cofnąć otwarcie?") },
        text = {
            Text(
                "Produkt „${budujTytul(produkt)}” zostanie oznaczony jako nieotwarty " +
                    "i wróci do zapasów. Czy na pewno?"
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Tak, cofnij") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

internal fun budujTytul(produkt: ProduktEntity): String {
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
