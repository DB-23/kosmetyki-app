package pl.bochynski.kosmetyki.ui.produkt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import java.time.LocalDate

data class ProduktFormUiState(
    val trybEdycji: Boolean = false,
    val trwaLadowanie: Boolean = true,
    val kategorie: List<KategoriaEntity> = emptyList(),
    val kategoriaId: Long? = null,
    val marka: String = "",
    val seria: String = "",
    val linia: String = "",
    val nazwa: String = "",
    val ean: String = "",
    val dataWaznosci: LocalDate? = null,
    val okresZuzycia: String = "",
    val jednostkaOkresuZuzycia: JednostkaOkresuZuzycia = JednostkaOkresuZuzycia.MIESIACE,
    val notatka: String = "",
    val liczbaSztuk: String = "1",
    val blad: String? = null,
    val zapisano: Boolean = false
)

class ProduktFormViewModel(
    private val produktId: Long?,
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModel() {

    private val _stan = MutableStateFlow(ProduktFormUiState(trybEdycji = produktId != null))
    val stan: StateFlow<ProduktFormUiState> = _stan.asStateFlow()

    private var oryginalnyProdukt: ProduktEntity? = null

    init {
        viewModelScope.launch {
            kategoriaRepository.obserwujKategorie().collect { kategorie ->
                _stan.update { it.copy(kategorie = kategorie) }
            }
        }
        if (produktId != null) {
            viewModelScope.launch {
                val produkt = produktRepository.obserwujProduktPoId(produktId).first()
                if (produkt != null) {
                    oryginalnyProdukt = produkt
                    _stan.update {
                        it.copy(
                            trwaLadowanie = false,
                            kategoriaId = produkt.kategoriaId,
                            marka = produkt.marka,
                            seria = produkt.seria.orEmpty(),
                            linia = produkt.linia.orEmpty(),
                            nazwa = produkt.nazwa,
                            ean = produkt.ean.orEmpty(),
                            dataWaznosci = produkt.dataWaznosci,
                            okresZuzycia = produkt.okresZuzyciaPoOtwarciu?.toString().orEmpty(),
                            jednostkaOkresuZuzycia = produkt.jednostkaOkresuZuzycia,
                            notatka = produkt.notatka.orEmpty()
                        )
                    }
                } else {
                    _stan.update { it.copy(trwaLadowanie = false) }
                }
            }
        } else {
            _stan.update { it.copy(trwaLadowanie = false) }
        }
    }

    fun ustawKategorie(id: Long) = _stan.update { it.copy(kategoriaId = id, blad = null) }
    fun ustawMarke(wartosc: String) = _stan.update { it.copy(marka = wartosc, blad = null) }
    fun ustawSerie(wartosc: String) = _stan.update { it.copy(seria = wartosc) }
    fun ustawLinie(wartosc: String) = _stan.update { it.copy(linia = wartosc) }
    fun ustawNazwe(wartosc: String) = _stan.update { it.copy(nazwa = wartosc, blad = null) }
    fun ustawEan(wartosc: String) = _stan.update { it.copy(ean = wartosc) }
    fun ustawDateWaznosci(data: LocalDate?) = _stan.update { it.copy(dataWaznosci = data) }
    fun ustawOkresZuzycia(wartosc: String) = _stan.update { it.copy(okresZuzycia = wartosc, blad = null) }
    fun ustawJednostkeOkresu(jednostka: JednostkaOkresuZuzycia) =
        _stan.update { it.copy(jednostkaOkresuZuzycia = jednostka) }
    fun ustawNotatke(wartosc: String) = _stan.update { it.copy(notatka = wartosc) }
    fun ustawLiczbeSztuk(wartosc: String) = _stan.update { it.copy(liczbaSztuk = wartosc, blad = null) }

    fun zapisz() {
        val aktualny = _stan.value

        if (aktualny.kategoriaId == null) {
            _stan.update { it.copy(blad = "Wybierz kategorię") }
            return
        }
        if (aktualny.marka.isBlank()) {
            _stan.update { it.copy(blad = "Podaj markę") }
            return
        }
        if (aktualny.nazwa.isBlank()) {
            _stan.update { it.copy(blad = "Podaj nazwę produktu") }
            return
        }
        val okresZuzycia = aktualny.okresZuzycia.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
        if (aktualny.okresZuzycia.isNotBlank() && okresZuzycia == null) {
            _stan.update { it.copy(blad = "Okres zużycia po otwarciu musi być liczbą całkowitą") }
            return
        }
        val liczbaSztuk = if (aktualny.trybEdycji) 1 else aktualny.liczbaSztuk.trim().toIntOrNull()
        if (!aktualny.trybEdycji && (liczbaSztuk == null || liczbaSztuk < 1)) {
            _stan.update { it.copy(blad = "Liczba sztuk musi być liczbą całkowitą większą od zera") }
            return
        }

        viewModelScope.launch {
            val bazowy = ProduktEntity(
                id = oryginalnyProdukt?.id ?: 0L,
                kategoriaId = aktualny.kategoriaId,
                marka = aktualny.marka.trim(),
                seria = aktualny.seria.trim().ifBlank { null },
                linia = aktualny.linia.trim().ifBlank { null },
                nazwa = aktualny.nazwa.trim(),
                ean = aktualny.ean.trim().ifBlank { null },
                zdjecieUri = oryginalnyProdukt?.zdjecieUri,
                dataWaznosci = aktualny.dataWaznosci,
                okresZuzyciaPoOtwarciu = okresZuzycia,
                jednostkaOkresuZuzycia = aktualny.jednostkaOkresuZuzycia,
                notatka = aktualny.notatka.trim().ifBlank { null }
            )

            val oryginal = oryginalnyProdukt
            if (aktualny.trybEdycji && oryginal != null) {
                produktRepository.aktualizuj(
                    bazowy.copy(
                        status = oryginal.status,
                        dataOtwarcia = oryginal.dataOtwarcia,
                        dataZuzycia = oryginal.dataZuzycia,
                        dataDodania = oryginal.dataDodania
                    )
                )
            } else {
                produktRepository.dodajWiele(
                    List(liczbaSztuk ?: 1) { bazowy.copy(dataDodania = LocalDate.now()) }
                )
            }
            _stan.update { it.copy(zapisano = true) }
        }
    }
}

class ProduktFormViewModelFactory(
    private val produktId: Long?,
    private val kategoriaRepository: KategoriaRepository,
    private val produktRepository: ProduktRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProduktFormViewModel(produktId, kategoriaRepository, produktRepository) as T
    }
}
