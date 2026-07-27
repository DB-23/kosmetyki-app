package pl.bochynski.kosmetyki.ui.sprawdzkod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.remote.OpenBeautyFactsApi
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.ui.historiacen.PunktCeny

data class SprawdzKodUiState(
    val ean: String = "",
    val trwaLadowanie: Boolean = true,
    val znaleziono: Boolean = false,
    val zrodloZewnetrzne: Boolean = false,
    val marka: String? = null,
    val nazwa: String? = null,
    val liczbaWZapasie: Int = 0,
    val liczbaOtwartych: Int = 0,
    val liczbaZuzytych: Int = 0,
    val liczbaLacznie: Int = 0,
    val sredniaCena: Double? = null,
    val najczestszeMiejsce: String? = null,
    val punkty: List<PunktCeny> = emptyList()
)

class SprawdzKodViewModel(
    ean: String,
    private val produktRepository: ProduktRepository
) : ViewModel() {

    private val _stan = MutableStateFlow(SprawdzKodUiState(ean = ean))
    val stan: StateFlow<SprawdzKodUiState> = _stan.asStateFlow()

    init {
        viewModelScope.launch {
            val lokalne = produktRepository.znajdzWszystkiePoEan(ean)
            if (lokalne.isNotEmpty()) {
                val ostatni = lokalne.maxBy { it.dataDodania }
                val punkty = lokalne
                    .filter { it.cenaZakupu != null && it.dataZakupu != null }
                    .map { PunktCeny(it.dataZakupu!!, it.cenaZakupu!!, it.miejsceZakupu) }
                    .sortedBy { it.data }
                val sredniaCena = punkty.map { it.cena }.takeIf { it.isNotEmpty() }
                    ?.let { it.sum() / it.size }
                val najczestszeMiejsce = punkty
                    .mapNotNull { it.miejsceZakupu?.takeIf { m -> m.isNotBlank() } }
                    .groupBy { it }
                    .maxByOrNull { it.value.size }
                    ?.key
                _stan.update {
                    it.copy(
                        trwaLadowanie = false,
                        znaleziono = true,
                        zrodloZewnetrzne = false,
                        marka = ostatni.marka,
                        nazwa = ostatni.nazwa,
                        liczbaWZapasie = lokalne.count { p -> p.status == StatusProduktu.W_ZAPASIE },
                        liczbaOtwartych = lokalne.count { p -> p.status == StatusProduktu.OTWARTE },
                        liczbaZuzytych = lokalne.count { p -> p.status == StatusProduktu.ZUZYTE },
                        liczbaLacznie = lokalne.size,
                        sredniaCena = sredniaCena,
                        najczestszeMiejsce = najczestszeMiejsce,
                        punkty = punkty
                    )
                }
            } else {
                val zApi = OpenBeautyFactsApi.pobierzProdukt(ean)
                _stan.update {
                    it.copy(
                        trwaLadowanie = false,
                        znaleziono = zApi != null,
                        zrodloZewnetrzne = zApi != null,
                        marka = zApi?.marka,
                        nazwa = zApi?.nazwa
                    )
                }
            }
        }
    }
}

class SprawdzKodViewModelFactory(
    private val ean: String,
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SprawdzKodViewModel(ean, produktRepository) as T
    }
}
