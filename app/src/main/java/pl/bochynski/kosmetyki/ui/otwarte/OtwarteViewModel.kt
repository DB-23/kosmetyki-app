package pl.bochynski.kosmetyki.ui.otwarte

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
import pl.bochynski.kosmetyki.domain.dniDoKonca
import java.time.LocalDate

data class OtwarteUiState(
    val produkty: List<ProduktEntity> = emptyList(),
    val nazwyKategorii: Map<Long, String> = emptyMap(),
    val trwaLadowanie: Boolean = true
)

class OtwarteViewModel(
    kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModel() {

    val stan: StateFlow<OtwarteUiState> = combine(
        kategoriaRepository.obserwujKategorie(),
        produktRepository.obserwujProduktyWgStatusu(StatusProduktu.OTWARTE)
    ) { kategorie, produkty ->
        OtwarteUiState(
            produkty = produkty.sortedWith(compareBy(nullsLast()) { it.dniDoKonca() }),
            nazwyKategorii = kategorie.associate { it.id to it.nazwa },
            trwaLadowanie = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtwarteUiState())

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

class OtwarteViewModelFactory(
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OtwarteViewModel(kategoriaRepository, produktRepository) as T
    }
}
