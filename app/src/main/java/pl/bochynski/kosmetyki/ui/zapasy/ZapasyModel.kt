package pl.bochynski.kosmetyki.ui.zapasy

import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import java.time.LocalDate

/**
 * Klucz identyczności fizycznych sztuk tego samego produktu — używany do zwijania
 * nieotwartych sztuk w jedną kartę z plakietką „×N".
 */
data class KluczGrupowania(
    val kategoriaId: Long,
    val marka: String,
    val seria: String?,
    val linia: String?,
    val nazwa: String,
    val ean: String?,
    val dataWaznosci: LocalDate?,
    val pao: Int?,
    val jednostkaPao: JednostkaOkresuZuzycia,
    val notatka: String?
)

fun ProduktEntity.kluczGrupowania() = KluczGrupowania(
    kategoriaId = kategoriaId,
    marka = marka,
    seria = seria,
    linia = linia,
    nazwa = nazwa,
    ean = ean,
    dataWaznosci = dataWaznosci,
    pao = okresZuzyciaPoOtwarciu,
    jednostkaPao = jednostkaOkresuZuzycia,
    notatka = notatka
)

/** Grupa jednej lub więcej fizycznie identycznych, nieotwartych sztuk (lub pojedyncza otwarta sztuka). */
data class GrupaProduktow(
    val klucz: KluczGrupowania,
    val produkty: List<ProduktEntity>
) {
    val reprezentant: ProduktEntity get() = produkty.first()
    val liczbaSztuk: Int get() = produkty.size

    /**
     * Unikalny klucz dla LazyColumn — sam [klucz] identyczności nie wystarcza, bo te same
     * atrybuty mogą mieć zarówno nieotwarte, jak i otwarte sztuki (osobne grupy, ten sam klucz).
     */
    val kluczListy: String get() = produkty.joinToString(",") { it.id.toString() }
}

data class ZapasyUiState(
    val kategorie: List<KategoriaUi> = emptyList(),
    val wybranaKategoriaId: Long? = null,
    val szukajTekst: String = "",
    val grupy: List<GrupaProduktow> = emptyList(),
    val trwaLadowanie: Boolean = true
)

data class KategoriaUi(val id: Long, val nazwa: String)
