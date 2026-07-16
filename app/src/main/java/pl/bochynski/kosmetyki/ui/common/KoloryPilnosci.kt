package pl.bochynski.kosmetyki.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pl.bochynski.kosmetyki.domain.PoziomPilnosci
import pl.bochynski.kosmetyki.ui.theme.KolorPilneTekst
import pl.bochynski.kosmetyki.ui.theme.KolorPilneTlo
import pl.bochynski.kosmetyki.ui.theme.KolorPrzeterminowaneTekst
import pl.bochynski.kosmetyki.ui.theme.KolorPrzeterminowaneTlo
import pl.bochynski.kosmetyki.ui.theme.KolorWkrotceTekst
import pl.bochynski.kosmetyki.ui.theme.KolorWkrotceTlo

data class KoloryKarty(val tlo: Color, val tekst: Color)

@Composable
fun koloryDlaPoziomu(poziom: PoziomPilnosci): KoloryKarty = when (poziom) {
    PoziomPilnosci.PRZETERMINOWANY -> KoloryKarty(KolorPrzeterminowaneTlo, KolorPrzeterminowaneTekst)
    PoziomPilnosci.PILNY -> KoloryKarty(KolorPilneTlo, KolorPilneTekst)
    PoziomPilnosci.WKROTCE -> KoloryKarty(KolorWkrotceTlo, KolorWkrotceTekst)
    PoziomPilnosci.NORMALNY, PoziomPilnosci.BEZ_TERMINU ->
        KoloryKarty(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
}
