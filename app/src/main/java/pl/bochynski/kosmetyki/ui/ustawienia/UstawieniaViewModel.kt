package pl.bochynski.kosmetyki.ui.ustawienia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository

data class UstawieniaUiState(
    val progDniTekst: String = "",
    val blad: String? = null,
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
}

class UstawieniaViewModelFactory(
    private val ustawieniaRepository: UstawieniaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UstawieniaViewModel(ustawieniaRepository) as T
    }
}
