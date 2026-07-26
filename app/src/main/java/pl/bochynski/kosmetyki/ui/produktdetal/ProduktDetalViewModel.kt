package pl.bochynski.kosmetyki.ui.produktdetal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository

data class ProduktDetalUiState(
    val produkt: ProduktEntity? = null,
    val nazwaKategorii: String? = null,
    val trwaLadowanie: Boolean = true
)

class ProduktDetalViewModel(
    private val produktId: Long,
    kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModel() {

    private val _stan = MutableStateFlow(ProduktDetalUiState())
    val stan: StateFlow<ProduktDetalUiState> = _stan.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                produktRepository.obserwujProduktPoId(produktId),
                kategoriaRepository.obserwujKategorie()
            ) { produkt, kategorie ->
                ProduktDetalUiState(
                    produkt = produkt,
                    nazwaKategorii = kategorie.firstOrNull { it.id == produkt?.kategoriaId }?.nazwa,
                    trwaLadowanie = false
                )
            }.collect { nowyStan -> _stan.value = nowyStan }
        }
    }

    fun przelaczUlubiony() {
        val produkt = _stan.value.produkt ?: return
        viewModelScope.launch {
            produktRepository.aktualizuj(produkt.copy(ulubiony = !produkt.ulubiony))
        }
    }
}

class ProduktDetalViewModelFactory(
    private val produktId: Long,
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProduktDetalViewModel(produktId, kategoriaRepository, produktRepository) as T
    }
}
