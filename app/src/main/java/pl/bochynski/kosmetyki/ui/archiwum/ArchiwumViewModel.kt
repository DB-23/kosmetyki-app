package pl.bochynski.kosmetyki.ui.archiwum

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

data class ArchiwumUiState(
    val produkty: List<ProduktEntity> = emptyList(),
    val nazwyKategorii: Map<Long, String> = emptyMap(),
    val trwaLadowanie: Boolean = true
)

class ArchiwumViewModel(
    kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModel() {

    val stan: StateFlow<ArchiwumUiState> = combine(
        kategoriaRepository.obserwujKategorie(),
        produktRepository.obserwujProduktyWgStatusu(StatusProduktu.ZUZYTE)
    ) { kategorie, produkty ->
        ArchiwumUiState(
            produkty = produkty.sortedByDescending { it.dataZuzycia },
            nazwyKategorii = kategorie.associate { it.id to it.nazwa },
            trwaLadowanie = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArchiwumUiState())

    fun przywrocDoZapasow(produkt: ProduktEntity) {
        if (produkt.status != StatusProduktu.ZUZYTE) return
        viewModelScope.launch {
            produktRepository.aktualizuj(
                produkt.copy(
                    status = if (produkt.dataOtwarcia != null) StatusProduktu.OTWARTE else StatusProduktu.W_ZAPASIE,
                    dataZuzycia = null
                )
            )
        }
    }

    fun usunTrwale(produkt: ProduktEntity) {
        viewModelScope.launch {
            produktRepository.usun(produkt)
        }
    }
}

class ArchiwumViewModelFactory(
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ArchiwumViewModel(kategoriaRepository, produktRepository) as T
    }
}
