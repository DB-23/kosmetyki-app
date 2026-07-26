package pl.bochynski.kosmetyki.ui.historiacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import java.time.LocalDate

data class KandydatHistorii(
    val marka: String,
    val seria: String?,
    val linia: String?,
    val nazwa: String
) {
    val etykieta: String
        get() {
            val naglowek = listOfNotNull(marka, seria, linia)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            return if (naglowek.isBlank()) nazwa else "$naglowek – $nazwa"
        }
}

data class PunktCeny(val data: LocalDate, val cena: Double)

data class HistoriaCenUiState(
    val kandydaci: List<KandydatHistorii> = emptyList(),
    val wybrany: KandydatHistorii? = null,
    val punkty: List<PunktCeny> = emptyList(),
    val trwaLadowanie: Boolean = true
)

private fun KandydatHistorii.odpowiadaProduktowi(produkt: ProduktEntity): Boolean =
    marka == produkt.marka && seria == produkt.seria && linia == produkt.linia && nazwa == produkt.nazwa

class HistoriaCenViewModel(
    private val produktRepository: ProduktRepository
) : ViewModel() {

    private val _stan = MutableStateFlow(HistoriaCenUiState())
    val stan: StateFlow<HistoriaCenUiState> = _stan.asStateFlow()

    private var wszystkieProdukty: List<ProduktEntity> = emptyList()

    init {
        viewModelScope.launch {
            produktRepository.obserwujWszystkieProdukty().collect { produkty ->
                wszystkieProdukty = produkty
                val kandydaci = produkty
                    .filter { it.cenaZakupu != null && it.dataZakupu != null }
                    .map { KandydatHistorii(it.marka, it.seria, it.linia, it.nazwa) }
                    .distinct()
                    .sortedWith(compareBy({ it.marka }, { it.nazwa }))
                val wybrany = _stan.value.wybrany?.takeIf { it in kandydaci } ?: kandydaci.firstOrNull()
                _stan.update {
                    it.copy(
                        kandydaci = kandydaci,
                        wybrany = wybrany,
                        punkty = punktyDla(wybrany),
                        trwaLadowanie = false
                    )
                }
            }
        }
    }

    fun wybierzProdukt(kandydat: KandydatHistorii) {
        _stan.update { it.copy(wybrany = kandydat, punkty = punktyDla(kandydat)) }
    }

    private fun punktyDla(kandydat: KandydatHistorii?): List<PunktCeny> {
        if (kandydat == null) return emptyList()
        return wszystkieProdukty
            .filter { kandydat.odpowiadaProduktowi(it) && it.cenaZakupu != null && it.dataZakupu != null }
            .map { PunktCeny(it.dataZakupu!!, it.cenaZakupu!!) }
            .sortedBy { it.data }
    }
}

class HistoriaCenViewModelFactory(
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoriaCenViewModel(produktRepository) as T
    }
}
