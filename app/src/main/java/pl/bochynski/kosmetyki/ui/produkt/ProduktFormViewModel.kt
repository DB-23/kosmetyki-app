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
    val pojemnosc: String = "",
    val dataWaznosci: LocalDate? = null,
    val okresZuzycia: String = "",
    val jednostkaOkresuZuzycia: JednostkaOkresuZuzycia = JednostkaOkresuZuzycia.MIESIACE,
    val dataZakupu: LocalDate? = null,
    val cenaZakupu: String = "",
    val miejsceZakupu: String = "",
    val podpowiedziMiejsc: List<String> = emptyList(),
    val podpowiedziMarek: List<String> = emptyList(),
    val podpowiedziNazw: List<String> = emptyList(),
    val notatka: String = "",
    val liczbaSztuk: String = "1",
    val blad: String? = null,
    val zapisano: Boolean = false,
    val kandydaciUzupelnienia: List<ProduktEntity> = emptyList()
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
        viewModelScope.launch {
            produktRepository.obserwujMiejscaZakupu().collect { miejsca ->
                _stan.update { it.copy(podpowiedziMiejsc = miejsca) }
            }
        }
        viewModelScope.launch {
            produktRepository.obserwujMarki().collect { marki ->
                _stan.update { it.copy(podpowiedziMarek = marki) }
            }
        }
        viewModelScope.launch {
            produktRepository.obserwujNazwy().collect { nazwy ->
                _stan.update { it.copy(podpowiedziNazw = nazwy) }
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
                            pojemnosc = produkt.pojemnosc.orEmpty(),
                            dataWaznosci = produkt.dataWaznosci,
                            okresZuzycia = produkt.okresZuzyciaPoOtwarciu?.toString().orEmpty(),
                            jednostkaOkresuZuzycia = produkt.jednostkaOkresuZuzycia,
                            dataZakupu = produkt.dataZakupu,
                            cenaZakupu = produkt.cenaZakupu?.toString().orEmpty(),
                            miejsceZakupu = produkt.miejsceZakupu.orEmpty(),
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

    fun wybierzPodpowiedzNazwy(nazwa: String) {
        _stan.update { it.copy(nazwa = nazwa, blad = null) }
        viewModelScope.launch {
            val znalezione = produktRepository.znajdzWszystkiePoNazwie(nazwa)
                .filter { it.id != oryginalnyProdukt?.id }
                .distinctBy {
                    listOf(
                        it.kategoriaId, it.marka, it.seria, it.linia, it.ean,
                        it.pojemnosc, it.okresZuzyciaPoOtwarciu, it.jednostkaOkresuZuzycia
                    )
                }
            if (znalezione.isNotEmpty()) {
                _stan.update { it.copy(kandydaciUzupelnienia = znalezione) }
            }
        }
    }

    fun uzupelnijZKandydata(kandydat: ProduktEntity) {
        _stan.update {
            it.copy(
                kategoriaId = kandydat.kategoriaId,
                marka = kandydat.marka,
                seria = kandydat.seria.orEmpty(),
                linia = kandydat.linia.orEmpty(),
                ean = kandydat.ean.orEmpty(),
                pojemnosc = kandydat.pojemnosc.orEmpty(),
                okresZuzycia = kandydat.okresZuzyciaPoOtwarciu?.toString().orEmpty(),
                jednostkaOkresuZuzycia = kandydat.jednostkaOkresuZuzycia,
                kandydaciUzupelnienia = emptyList()
            )
        }
    }

    fun odrzucUzupelnienieDanych() = _stan.update { it.copy(kandydaciUzupelnienia = emptyList()) }
    fun ustawEan(wartosc: String) = _stan.update { it.copy(ean = wartosc) }
    fun ustawPojemnosc(wartosc: String) = _stan.update { it.copy(pojemnosc = wartosc) }
    fun ustawDateWaznosci(data: LocalDate?) = _stan.update { it.copy(dataWaznosci = data) }
    fun ustawOkresZuzycia(wartosc: String) = _stan.update { it.copy(okresZuzycia = wartosc, blad = null) }
    fun ustawJednostkeOkresu(jednostka: JednostkaOkresuZuzycia) =
        _stan.update { it.copy(jednostkaOkresuZuzycia = jednostka) }
    fun ustawDateZakupu(data: LocalDate?) = _stan.update { it.copy(dataZakupu = data) }
    fun ustawCeneZakupu(wartosc: String) = _stan.update { it.copy(cenaZakupu = wartosc, blad = null) }
    fun ustawMiejsceZakupu(wartosc: String) = _stan.update { it.copy(miejsceZakupu = wartosc) }
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
        val cenaZakupu = aktualny.cenaZakupu.trim().replace(',', '.').takeIf { it.isNotBlank() }?.toDoubleOrNull()
        if (aktualny.cenaZakupu.isNotBlank() && cenaZakupu == null) {
            _stan.update { it.copy(blad = "Cena zakupu musi być liczbą") }
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
                pojemnosc = aktualny.pojemnosc.trim().ifBlank { null },
                dataWaznosci = aktualny.dataWaznosci,
                okresZuzyciaPoOtwarciu = okresZuzycia,
                jednostkaOkresuZuzycia = aktualny.jednostkaOkresuZuzycia,
                dataZakupu = aktualny.dataZakupu,
                cenaZakupu = cenaZakupu,
                miejsceZakupu = aktualny.miejsceZakupu.trim().ifBlank { null },
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
