package pl.bochynski.kosmetyki.ui.ustawienia

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.bochynski.kosmetyki.data.backup.KopiaZapasowaProduktow
import pl.bochynski.kosmetyki.data.repository.DOMYSLNE_KOLORY_STATUSOW
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.KoloryStatusow
import pl.bochynski.kosmetyki.data.repository.KonfiguracjaSerweraBazy
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.data.repository.StatusKolorowy
import pl.bochynski.kosmetyki.data.repository.TrybMotywu
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository

data class UstawieniaUiState(
    val progDniTekst: String = "",
    val blad: String? = null,
    val trybMotywu: TrybMotywu = TrybMotywu.SYSTEMOWY,
    val koloryStatusow: KoloryStatusow = DOMYSLNE_KOLORY_STATUSOW,
    val konfiguracjaSerwera: KonfiguracjaSerweraBazy = KonfiguracjaSerweraBazy(),
    val trwaLadowanie: Boolean = true,
    val trwaOperacjaBazy: Boolean = false,
    val komunikatBazy: String? = null
)

class UstawieniaViewModel(
    private val ustawieniaRepository: UstawieniaRepository,
    private val produktRepository: ProduktRepository,
    private val kategoriaRepository: KategoriaRepository
) : ViewModel() {

    private val _stan = MutableStateFlow(UstawieniaUiState())
    val stan: StateFlow<UstawieniaUiState> = _stan.asStateFlow()

    init {
        viewModelScope.launch {
            val prog = ustawieniaRepository.pobierzProgPowiadomienDni()
            _stan.update { it.copy(progDniTekst = prog.toString(), trwaLadowanie = false) }
        }
        viewModelScope.launch {
            ustawieniaRepository.obserwujTrybMotywu().collect { tryb ->
                _stan.update { it.copy(trybMotywu = tryb) }
            }
        }
        viewModelScope.launch {
            ustawieniaRepository.obserwujKoloryStatusow().collect { kolory ->
                _stan.update { it.copy(koloryStatusow = kolory) }
            }
        }
        viewModelScope.launch {
            ustawieniaRepository.obserwujKonfiguracjeSerwera().collect { konfiguracja ->
                _stan.update { it.copy(konfiguracjaSerwera = konfiguracja) }
            }
        }
    }

    fun ustawProgDni(wartosc: String) {
        _stan.update { it.copy(progDniTekst = wartosc) }
        val liczba = wartosc.trim().toIntOrNull()
        if (liczba == null || liczba <= 0) {
            _stan.update {
                it.copy(blad = if (wartosc.isBlank()) null else "Podaj liczbę całkowitą większą od zera")
            }
            return
        }
        _stan.update { it.copy(blad = null) }
        viewModelScope.launch { ustawieniaRepository.ustawProgPowiadomienDni(liczba) }
    }

    fun ustawTrybMotywu(tryb: TrybMotywu) {
        viewModelScope.launch { ustawieniaRepository.ustawTrybMotywu(tryb) }
    }

    fun ustawKolorStatusu(status: StatusKolorowy, kolorArgb: Int) {
        viewModelScope.launch { ustawieniaRepository.ustawKolorStatusu(status, kolorArgb) }
    }

    fun ustawAdresSerwera(wartosc: String) {
        viewModelScope.launch { ustawieniaRepository.ustawAdresSerwera(wartosc) }
    }

    fun ustawPortSerwera(wartosc: String) {
        viewModelScope.launch { ustawieniaRepository.ustawPortSerwera(wartosc) }
    }

    fun ustawNazweUzytkownikaSerwera(wartosc: String) {
        viewModelScope.launch { ustawieniaRepository.ustawNazweUzytkownikaSerwera(wartosc) }
    }

    fun ustawHasloSerwera(wartosc: String) {
        viewModelScope.launch { ustawieniaRepository.ustawHasloSerwera(wartosc) }
    }

    fun wyzerujBaze() {
        _stan.update { it.copy(trwaOperacjaBazy = true, komunikatBazy = null) }
        viewModelScope.launch {
            produktRepository.usunWszystkie()
            _stan.update {
                it.copy(trwaOperacjaBazy = false, komunikatBazy = "Baza danych została wyzerowana.")
            }
        }
    }

    fun eksportujDoPliku(resolver: ContentResolver, uri: Uri) {
        _stan.update { it.copy(trwaOperacjaBazy = true, komunikatBazy = null) }
        viewModelScope.launch {
            val wynik = runCatching {
                val produkty = produktRepository.obserwujWszystkieProdukty().first()
                val kategorie = kategoriaRepository.obserwujKategorie().first()
                val tekst = KopiaZapasowaProduktow.zserializuj(produkty, kategorie)
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { strumien ->
                        strumien.write(tekst.toByteArray())
                    } ?: error("Nie udało się otworzyć pliku do zapisu")
                }
                produkty.size
            }
            _stan.update {
                it.copy(
                    trwaOperacjaBazy = false,
                    komunikatBazy = wynik.fold(
                        onSuccess = { liczba -> "Wyeksportowano $liczba produktów do pliku." },
                        onFailure = { blad -> "Eksport nie powiódł się: ${blad.message}" }
                    )
                )
            }
        }
    }

    fun importujZPliku(resolver: ContentResolver, uri: Uri) {
        _stan.update { it.copy(trwaOperacjaBazy = true, komunikatBazy = null) }
        viewModelScope.launch {
            val wynik = runCatching {
                val tekst = withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Nie udało się otworzyć pliku do odczytu")
                }
                val kategorie = kategoriaRepository.obserwujKategorie().first()
                val odczyt = KopiaZapasowaProduktow.odczytaj(tekst, kategorie)
                if (odczyt.produkty.isNotEmpty()) {
                    produktRepository.dodajWiele(odczyt.produkty)
                }
                odczyt
            }
            _stan.update {
                it.copy(
                    trwaOperacjaBazy = false,
                    komunikatBazy = wynik.fold(
                        onSuccess = { odczyt ->
                            val podstawowy = "Zaimportowano ${odczyt.produkty.size} produktów."
                            if (odczyt.pominieteBrakKategorii > 0) {
                                "$podstawowy Pominięto ${odczyt.pominieteBrakKategorii} " +
                                    "(nierozpoznana kategoria)."
                            } else {
                                podstawowy
                            }
                        },
                        onFailure = { blad -> "Import nie powiódł się: ${blad.message}" }
                    )
                )
            }
        }
    }

    fun wyczyscKomunikatBazy() = _stan.update { it.copy(komunikatBazy = null) }
}

class UstawieniaViewModelFactory(
    private val ustawieniaRepository: UstawieniaRepository,
    private val produktRepository: ProduktRepository,
    private val kategoriaRepository: KategoriaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UstawieniaViewModel(ustawieniaRepository, produktRepository, kategoriaRepository) as T
    }
}
