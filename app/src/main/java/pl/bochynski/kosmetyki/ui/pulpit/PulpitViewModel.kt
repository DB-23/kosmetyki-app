package pl.bochynski.kosmetyki.ui.pulpit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.domain.PoziomPilnosci
import pl.bochynski.kosmetyki.domain.dniDoKonca
import pl.bochynski.kosmetyki.domain.poziomPilnosci

data class PulpitUiState(
    val liczbaWszystkich: Int = 0,
    val liczbaWZapasie: Int = 0,
    val liczbaOtwartych: Int = 0,
    val liczbaPrzeterminowanych: Int = 0,
    val liczbaPilnych: Int = 0,
    val liczbaWkrotce: Int = 0,
    val wartoscZapasow: Double = 0.0,
    val sredniaCena: Double? = null,
    val najczestszaMarka: String? = null,
    val najczestszeMiejsceZakupu: String? = null,
    val trwaLadowanie: Boolean = true
)

class PulpitViewModel(
    private val produktRepository: ProduktRepository
) : ViewModel() {

    private val _stan = MutableStateFlow(PulpitUiState())
    val stan: StateFlow<PulpitUiState> = _stan.asStateFlow()

    init {
        viewModelScope.launch {
            produktRepository.obserwujWszystkieProdukty().collect { produkty ->
                val aktywne = produkty.filter { it.status != StatusProduktu.ZUZYTE }
                val poziomy = aktywne.map { poziomPilnosci(it.dniDoKonca()) }
                _stan.update {
                    it.copy(
                        liczbaWszystkich = aktywne.size,
                        liczbaWZapasie = aktywne.count { p -> p.status == StatusProduktu.W_ZAPASIE },
                        liczbaOtwartych = aktywne.count { p -> p.status == StatusProduktu.OTWARTE },
                        liczbaPrzeterminowanych = poziomy.count { p -> p == PoziomPilnosci.PRZETERMINOWANY },
                        liczbaPilnych = poziomy.count { p -> p == PoziomPilnosci.PILNY },
                        liczbaWkrotce = poziomy.count { p -> p == PoziomPilnosci.WKROTCE },
                        wartoscZapasow = aktywne.sumOf { p -> p.cenaZakupu ?: 0.0 },
                        sredniaCena = produkty.mapNotNull { p -> p.cenaZakupu }.takeIf { it.isNotEmpty() }
                            ?.let { ceny -> ceny.sum() / ceny.size },
                        najczestszaMarka = produkty
                            .groupBy { p -> p.marka }
                            .maxByOrNull { entry -> entry.value.size }
                            ?.key,
                        najczestszeMiejsceZakupu = produkty
                            .mapNotNull { p -> p.miejsceZakupu?.takeIf { m -> m.isNotBlank() } }
                            .groupBy { it }
                            .maxByOrNull { entry -> entry.value.size }
                            ?.key,
                        trwaLadowanie = false
                    )
                }
            }
        }
    }
}

class PulpitViewModelFactory(
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PulpitViewModel(produktRepository) as T
    }
}
