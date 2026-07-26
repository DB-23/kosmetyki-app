package pl.bochynski.kosmetyki.ui.filtrowanalista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.domain.PoziomPilnosci
import pl.bochynski.kosmetyki.domain.dniDoKonca
import pl.bochynski.kosmetyki.domain.poziomPilnosci
import java.time.LocalDate

enum class RodzajFiltra(val tytul: String) {
    WSZYSTKIE("Wszystkie kosmetyki"),
    PRZETERMINOWANE("Przeterminowane"),
    PILNE("Termin w ciągu 90 dni"),
    WKROTCE("Termin w ciągu 180 dni"),
    W_ZAPASIE("W zapasie")
}

data class FiltrowanaListaUiState(
    val tytul: String = "",
    val produkty: List<ProduktEntity> = emptyList(),
    val nazwyKategorii: Map<Long, String> = emptyMap(),
    val trwaLadowanie: Boolean = true
)

private fun pasujeDoFiltra(produkt: ProduktEntity, rodzaj: RodzajFiltra): Boolean {
    if (rodzaj == RodzajFiltra.W_ZAPASIE) {
        return produkt.status == StatusProduktu.W_ZAPASIE
    }
    if (produkt.status == StatusProduktu.ZUZYTE) return false
    return when (rodzaj) {
        RodzajFiltra.WSZYSTKIE -> true
        RodzajFiltra.PRZETERMINOWANE -> poziomPilnosci(produkt.dniDoKonca()) == PoziomPilnosci.PRZETERMINOWANY
        RodzajFiltra.PILNE -> poziomPilnosci(produkt.dniDoKonca()) == PoziomPilnosci.PILNY
        RodzajFiltra.WKROTCE -> poziomPilnosci(produkt.dniDoKonca()) == PoziomPilnosci.WKROTCE
        RodzajFiltra.W_ZAPASIE -> true
    }
}

class FiltrowanaListaViewModel(
    private val rodzaj: RodzajFiltra,
    kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModel() {

    val stan: StateFlow<FiltrowanaListaUiState> = combine(
        kategoriaRepository.obserwujKategorie(),
        produktRepository.obserwujWszystkieProdukty()
    ) { kategorie, produkty ->
        FiltrowanaListaUiState(
            tytul = rodzaj.tytul,
            produkty = produkty
                .filter { pasujeDoFiltra(it, rodzaj) }
                .sortedWith(compareBy(nullsLast()) { it.dniDoKonca() }),
            nazwyKategorii = kategorie.associate { it.id to it.nazwa },
            trwaLadowanie = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FiltrowanaListaUiState(tytul = rodzaj.tytul)
    )

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

    fun oznaczZuzyte(produkt: ProduktEntity) {
        if (produkt.status == StatusProduktu.ZUZYTE) return
        viewModelScope.launch {
            produktRepository.aktualizuj(
                produkt.copy(status = StatusProduktu.ZUZYTE, dataZuzycia = LocalDate.now())
            )
        }
    }
}

class FiltrowanaListaViewModelFactory(
    private val rodzaj: RodzajFiltra,
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FiltrowanaListaViewModel(rodzaj, kategoriaRepository, produktRepository) as T
    }
}
