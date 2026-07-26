package pl.bochynski.kosmetyki.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import pl.bochynski.kosmetyki.data.repository.DOMYSLNE_KOLORY_STATUSOW
import pl.bochynski.kosmetyki.domain.PoziomPilnosci

data class KoloryKarty(val tlo: Color, val tekst: Color)

val LocalKoloryStatusow = compositionLocalOf { DOMYSLNE_KOLORY_STATUSOW }

@Composable
fun koloryDlaPoziomu(poziom: PoziomPilnosci): KoloryKarty {
    val koloryStatusow = LocalKoloryStatusow.current
    return when (poziom) {
        PoziomPilnosci.PRZETERMINOWANY -> koloryDlaTla(koloryStatusow.przeterminowane)
        PoziomPilnosci.PILNY -> koloryDlaTla(koloryStatusow.pilne)
        PoziomPilnosci.WKROTCE -> koloryDlaTla(koloryStatusow.wkrotce)
        PoziomPilnosci.NORMALNY, PoziomPilnosci.BEZ_TERMINU ->
            KoloryKarty(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
    }
}

private fun koloryDlaTla(kolorArgb: Int): KoloryKarty {
    val tlo = Color(kolorArgb)
    val tekst = if (tlo.luminance() > 0.5f) Color.Black else Color.White
    return KoloryKarty(tlo, tekst)
}
