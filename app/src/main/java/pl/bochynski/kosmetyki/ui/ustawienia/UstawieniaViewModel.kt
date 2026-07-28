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
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity
import pl.bochynski.kosmetyki.data.repository.DOMYSLNE_KOLORY_STATUSOW
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.KoloryStatusow
import pl.bochynski.kosmetyki.data.repository.KonfiguracjaSerweraBazy
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.data.repository.StatusKolorowy
import pl.bochynski.kosmetyki.data.repository.TrybMotywu
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository
import pl.bochynski.kosmetyki.data.seed.DatabaseSeeder

enum class TrybImportu { DODAJ, ZASTAP }

data class UstawieniaUiState(
    val progDniTekst: String = "",
    val blad: String? = null,
    val trybMotywu: TrybMotywu = TrybMotywu.SYSTEMOWY,
    val koloryStatusow: KoloryStatusow = DOMYSLNE_KOLORY_STATUSOW,
    val konfiguracjaSerwera: KonfiguracjaSerweraBazy = KonfiguracjaSerweraBazy(),
    val kategorie: List<KategoriaEntity> = emptyList(),
    val bladKategorii: String? = null,
    val trwaLadowanie: Boolean = true,
    val trwaOperacjaBazy: Boolean = false,
    val komunikatBazy: String? = null
)

class UstawieniaViewModel(
    private val ustawieniaRepository: UstawieniaRepository,
    private val produktRepository: ProduktRepository,
    private val kategoriaRepository: KategoriaRepository,
    private val databaseSeeder: DatabaseSeeder
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
        viewModelScope.launch {
            kategoriaRepository.obserwujKategorie().collect { kategorie ->
                _stan.update { it.copy(kategorie = kategorie) }
            }
        }
    }

    fun dodajKategorie(nazwa: String) {
        if (nazwa.isBlank()) return
        viewModelScope.launch {
            val dodano = kategoriaRepository.dodaj(nazwa)
            _stan.update {
                it.copy(bladKategorii = if (dodano) null else "Kategoria o tej nazwie już istnieje")
            }
        }
    }

    fun wyczyscBladKategorii() = _stan.update { it.copy(bladKategorii = null) }

    fun zmienNazweKategorii(kategoria: KategoriaEntity, nowaNazwa: String) {
        viewModelScope.launch {
            val powiodlo = kategoriaRepository.zmienNazwe(kategoria, nowaNazwa)
            _stan.update {
                it.copy(bladKategorii = if (powiodlo) null else "Kategoria o tej nazwie już istnieje")
            }
        }
    }

    fun usunKategorie(kategoria: KategoriaEntity) {
        viewModelScope.launch {
            val liczbaProduktow = kategoriaRepository.usun(kategoria)
            _stan.update {
                it.copy(
                    bladKategorii = if (liczbaProduktow > 0) {
                        "Nie można usunąć „${kategoria.nazwa}” — jest używana przez " +
                            "$liczbaProduktow produktów"
                    } else {
                        null
                    }
                )
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
            val wynik = runCatching { produktRepository.usunWszystkie() }
            _stan.update {
                it.copy(
                    trwaOperacjaBazy = false,
                    komunikatBazy = wynik.fold(
                        onSuccess = { "Baza danych została wyzerowana." },
                        onFailure = { blad -> "Zerowanie bazy nie powiodło się: ${blad.message}" }
                    )
                )
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

    fun importujZPliku(resolver: ContentResolver, uri: Uri, tryb: TrybImportu) {
        _stan.update { it.copy(trwaOperacjaBazy = true, komunikatBazy = null) }
        viewModelScope.launch {
            val wynik = runCatching {
                val tekst = withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Nie udało się otworzyć pliku do odczytu")
                }
                val kategorie = kategoriaRepository.obserwujKategorie().first()
                val odczyt = KopiaZapasowaProduktow.odczytaj(tekst, kategorie)
                if (tryb == TrybImportu.ZASTAP) {
                    produktRepository.usunWszystkie()
                }
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
                            val czynnosc = if (tryb == TrybImportu.ZASTAP) {
                                "Zastąpiono bazę"
                            } else {
                                "Zaimportowano"
                            }
                            val podstawowy = "$czynnosc ${odczyt.produkty.size} produktów."
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

    fun wczytajBazeDemonstracyjna() {
        _stan.update { it.copy(trwaOperacjaBazy = true, komunikatBazy = null) }
        viewModelScope.launch {
            val wynik = runCatching { databaseSeeder.wczytajDemo() }
            _stan.update {
                it.copy(
                    trwaOperacjaBazy = false,
                    komunikatBazy = wynik.fold(
                        onSuccess = { liczba -> "Wczytano bazę demonstracyjną: $liczba produktów." },
                        onFailure = { blad -> "Wczytanie bazy demonstracyjnej nie powiodło się: ${blad.message}" }
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
    private val kategoriaRepository: KategoriaRepository,
    private val databaseSeeder: DatabaseSeeder
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UstawieniaViewModel(ustawieniaRepository, produktRepository, kategoriaRepository, databaseSeeder) as T
    }
}
