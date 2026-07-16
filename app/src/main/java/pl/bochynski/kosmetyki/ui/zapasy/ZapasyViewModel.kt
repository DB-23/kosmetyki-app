package pl.bochynski.kosmetyki.ui.zapasy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.domain.dniDoKonca
import java.time.LocalDate

class ZapasyViewModel(
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModel() {

    private val wybranaKategoriaId = MutableStateFlow<Long?>(null)
    private val szukajTekst = MutableStateFlow("")

    val stan: StateFlow<ZapasyUiState> = combine(
        kategoriaRepository.obserwujKategorie(),
        produktRepository.obserwujWszystkieProdukty(),
        wybranaKategoriaId,
        szukajTekst
    ) { kategorie, produkty, wybranaId, tekst ->
        val kategorieUi = kategorie.map { KategoriaUi(it.id, it.nazwa) }
        val przefiltrowane = produkty
            .asSequence()
            .filter { it.status != StatusProduktu.ZUZYTE }
            .filter { wybranaId == null || it.kategoriaId == wybranaId }
            .filter { pasujeDoSzukania(it, tekst) }
            .toList()

        ZapasyUiState(
            kategorie = kategorieUi,
            wybranaKategoriaId = wybranaId,
            szukajTekst = tekst,
            grupy = zbudujGrupy(przefiltrowane),
            trwaLadowanie = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ZapasyUiState())

    fun wybierzKategorie(kategoriaId: Long?) {
        wybranaKategoriaId.value = kategoriaId
    }

    fun ustawSzukajTekst(tekst: String) {
        szukajTekst.value = tekst
    }

    fun oznaczOtwarte(produkt: ProduktEntity) {
        if (produkt.status != StatusProduktu.W_ZAPASIE) return
        viewModelScope.launch {
            produktRepository.aktualizuj(
                produkt.copy(status = StatusProduktu.OTWARTE, dataOtwarcia = LocalDate.now())
            )
        }
    }

    fun cofnijOtwarcie(produkt: ProduktEntity) {
        if (produkt.status != StatusProduktu.OTWARTE) return
        viewModelScope.launch {
            produktRepository.aktualizuj(
                produkt.copy(status = StatusProduktu.W_ZAPASIE, dataOtwarcia = null)
            )
        }
    }

    private fun pasujeDoSzukania(produkt: ProduktEntity, tekst: String): Boolean {
        if (tekst.isBlank()) return true
        val zapytanie = tekst.trim()
        return listOfNotNull(produkt.marka, produkt.seria, produkt.linia, produkt.nazwa)
            .any { it.contains(zapytanie, ignoreCase = true) }
    }

    private fun zbudujGrupy(produkty: List<ProduktEntity>): List<GrupaProduktow> {
        val nieotwarte = produkty.filter { it.status == StatusProduktu.W_ZAPASIE }
            .groupBy { it.kluczGrupowania() }
            .map { (klucz, sztuki) -> GrupaProduktow(klucz, sztuki) }
            .sortedWith(compareBy(nullsLast()) { it.reprezentant.dniDoKonca() })

        val otwarte = produkty.filter { it.status == StatusProduktu.OTWARTE }
            .map { GrupaProduktow(it.kluczGrupowania(), listOf(it)) }
            .sortedWith(compareBy(nullsLast()) { it.reprezentant.dniDoKonca() })

        return nieotwarte + otwarte
    }
}

class ZapasyViewModelFactory(
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ZapasyViewModel(kategoriaRepository, produktRepository) as T
    }
}
