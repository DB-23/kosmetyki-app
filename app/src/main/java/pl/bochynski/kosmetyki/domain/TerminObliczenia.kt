package pl.bochynski.kosmetyki.domain

import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Wcześniejsza z dat: dataWaznosci oraz (dataOtwarcia + okres zużycia po otwarciu), jeśli produkt otwarty.
 * Null, gdy żadna z dat nie jest znana.
 */
fun ProduktEntity.terminEfektywny(): LocalDate? {
    val terminPoOtwarciu = if (status == StatusProduktu.OTWARTE && dataOtwarcia != null && okresZuzyciaPoOtwarciu != null) {
        when (jednostkaOkresuZuzycia) {
            JednostkaOkresuZuzycia.MIESIACE -> dataOtwarcia.plusMonths(okresZuzyciaPoOtwarciu.toLong())
            JednostkaOkresuZuzycia.DNI -> dataOtwarcia.plusDays(okresZuzyciaPoOtwarciu.toLong())
        }
    } else {
        null
    }
    return listOfNotNull(dataWaznosci, terminPoOtwarciu).minOrNull()
}

fun ProduktEntity.dniDoKonca(dzisiaj: LocalDate = LocalDate.now()): Long? =
    terminEfektywny()?.let { ChronoUnit.DAYS.between(dzisiaj, it) }

enum class PoziomPilnosci {
    PRZETERMINOWANY,
    PILNY,
    WKROTCE,
    NORMALNY,
    BEZ_TERMINU
}

fun poziomPilnosci(dniDoKonca: Long?): PoziomPilnosci = when {
    dniDoKonca == null -> PoziomPilnosci.BEZ_TERMINU
    dniDoKonca <= 0 -> PoziomPilnosci.PRZETERMINOWANY
    dniDoKonca < 90 -> PoziomPilnosci.PILNY
    dniDoKonca < 180 -> PoziomPilnosci.WKROTCE
    else -> PoziomPilnosci.NORMALNY
}
