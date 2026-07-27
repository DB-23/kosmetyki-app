package pl.bochynski.kosmetyki.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.bochynski.kosmetyki.ui.historiacen.PunktCeny
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMAT_DATY_WYKRESU = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val LOCALE_PL_WYKRESU: Locale = Locale.Builder().setLanguage("pl").setRegion("PL").build()

private fun formatujCeneWykresu(wartosc: Double): String =
    String.format(LOCALE_PL_WYKRESU, "%.2f zł", wartosc)

@Composable
fun WykresCen(punkty: List<PunktCeny>, modifier: Modifier = Modifier) {
    val minCena = punkty.minOf { it.cena }
    val maxCena = punkty.maxOf { it.cena }
    val sredniaSkali = (minCena + maxCena) / 2

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatujCeneWykresu(maxCena), style = MaterialTheme.typography.labelSmall)
                Text(formatujCeneWykresu(sredniaSkali), style = MaterialTheme.typography.labelSmall)
                Text(formatujCeneWykresu(minCena), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.width(8.dp))
            WykresCanvas(
                punkty = punkty,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp)
        ) {
            Text(
                punkty.first().data.format(FORMAT_DATY_WYKRESU),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                punkty.last().data.format(FORMAT_DATY_WYKRESU),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WykresCanvas(punkty: List<PunktCeny>, modifier: Modifier = Modifier) {
    val kolorLinii = MaterialTheme.colorScheme.primary
    val kolorSiatki = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        val minCena = punkty.minOf { it.cena }
        val maxCena = punkty.maxOf { it.cena }
        val zakresCeny = (maxCena - minCena).takeIf { it > 0.0 } ?: 1.0
        val minDzien = punkty.first().data.toEpochDay()
        val maxDzien = punkty.last().data.toEpochDay()
        val zakresDni = (maxDzien - minDzien).takeIf { it > 0L } ?: 1L

        val paddingPx = 8.dp.toPx()
        val szerokosc = size.width - 2 * paddingPx
        val wysokosc = size.height - 2 * paddingPx

        listOf(0f, 0.5f, 1f).forEach { udzial ->
            val y = paddingPx + udzial * wysokosc
            drawLine(
                color = kolorSiatki,
                start = Offset(paddingPx, y),
                end = Offset(paddingPx + szerokosc, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }

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
