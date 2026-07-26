package pl.bochynski.kosmetyki.ui.ustawienia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.repository.DOMYSLNE_KOLORY_STATUSOW
import pl.bochynski.kosmetyki.data.repository.KoloryStatusow
import pl.bochynski.kosmetyki.data.repository.StatusKolorowy
import pl.bochynski.kosmetyki.data.repository.TrybMotywu
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository

data class UstawieniaUiState(
    val progDniTekst: String = "",
    val blad: String? = null,
    val trybMotywu: TrybMotywu = TrybMotywu.SYSTEMOWY,
    val koloryStatusow: KoloryStatusow = DOMYSLNE_KOLORY_STATUSOW,
    val trwaLadowanie: Boolean = true
)

class UstawieniaViewModel(
    private val ustawieniaRepository: UstawieniaRepository
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
}

class UstawieniaViewModelFactory(
    private val ustawieniaRepository: UstawieniaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UstawieniaViewModel(ustawieniaRepository) as T
    }
}
