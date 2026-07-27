package pl.bochynski.kosmetyki.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

private const val PLIK_USTAWIEN = "ustawienia"
private const val KLUCZ_PROG_POWIADOMIEN_DNI = "prog_powiadomien_dni"
private const val KLUCZ_TRYB_MOTYWU = "tryb_motywu"
private const val KLUCZ_KOLOR_PRZETERMINOWANE = "kolor_przeterminowane"
private const val KLUCZ_KOLOR_PILNE = "kolor_pilne"
private const val KLUCZ_KOLOR_WKROTCE = "kolor_wkrotce"
private const val KLUCZ_SERWER_ADRES = "serwer_bazy_adres"
private const val KLUCZ_SERWER_PORT = "serwer_bazy_port"
private const val KLUCZ_SERWER_NAZWA_UZYTKOWNIKA = "serwer_bazy_nazwa_uzytkownika"
private const val KLUCZ_SERWER_HASLO = "serwer_bazy_haslo"
const val DOMYSLNY_PROG_POWIADOMIEN_DNI = 90

enum class TrybMotywu { SYSTEMOWY, JASNY, CIEMNY }

enum class StatusKolorowy { PRZETERMINOWANE, PILNE, WKROTCE }

data class KoloryStatusow(
    val przeterminowane: Int,
    val pilne: Int,
    val wkrotce: Int
)

val DOMYSLNE_KOLORY_STATUSOW = KoloryStatusow(
    przeterminowane = 0xFF7F0000.toInt(),
    pilne = 0xFFD32F2F.toInt(),
    wkrotce = 0xFFF9A825.toInt()
)

// Szkielet pod przyszle polaczenie z zewnetrznym serwerem bazy produktow (kody EAN).
// Na razie wartosci sa tylko zapisywane lokalnie - nic nie laczy sie z siecia.
data class KonfiguracjaSerweraBazy(
    val adres: String = "",
    val port: String = "",
    val nazwaUzytkownika: String = "",
    val haslo: String = ""
)

interface UstawieniaRepository {
    suspend fun pobierzProgPowiadomienDni(): Int
    suspend fun ustawProgPowiadomienDni(dni: Int)
    fun obserwujTrybMotywu(): StateFlow<TrybMotywu>
    suspend fun ustawTrybMotywu(tryb: TrybMotywu)
    fun obserwujKoloryStatusow(): StateFlow<KoloryStatusow>
    suspend fun ustawKolorStatusu(status: StatusKolorowy, kolorArgb: Int)
    fun obserwujKonfiguracjeSerwera(): StateFlow<KonfiguracjaSerweraBazy>
    suspend fun ustawAdresSerwera(wartosc: String)
    suspend fun ustawPortSerwera(wartosc: String)
    suspend fun ustawNazweUzytkownikaSerwera(wartosc: String)
    suspend fun ustawHasloSerwera(wartosc: String)
}

class UstawieniaRepositoryImpl(
    context: Context
) : UstawieniaRepository {
    private val preferencje = context.getSharedPreferences(PLIK_USTAWIEN, Context.MODE_PRIVATE)

    private val _trybMotywu = MutableStateFlow(wczytajTrybMotywu())
    private val _koloryStatusow = MutableStateFlow(wczytajKoloryStatusow())
    private val _konfiguracjaSerwera = MutableStateFlow(wczytajKonfiguracjeSerwera())

    override suspend fun pobierzProgPowiadomienDni(): Int = withContext(Dispatchers.IO) {
        preferencje.getInt(KLUCZ_PROG_POWIADOMIEN_DNI, DOMYSLNY_PROG_POWIADOMIEN_DNI)
    }

    override suspend fun ustawProgPowiadomienDni(dni: Int) = withContext(Dispatchers.IO) {
        preferencje.edit().putInt(KLUCZ_PROG_POWIADOMIEN_DNI, dni).apply()
    }

    override fun obserwujTrybMotywu(): StateFlow<TrybMotywu> = _trybMotywu.asStateFlow()

    override suspend fun ustawTrybMotywu(tryb: TrybMotywu) = withContext(Dispatchers.IO) {
        preferencje.edit().putString(KLUCZ_TRYB_MOTYWU, tryb.name).apply()
        _trybMotywu.value = tryb
    }

    override fun obserwujKoloryStatusow(): StateFlow<KoloryStatusow> = _koloryStatusow.asStateFlow()

    override suspend fun ustawKolorStatusu(status: StatusKolorowy, kolorArgb: Int) = withContext(Dispatchers.IO) {
        val klucz = kluczKoloru(status)
        preferencje.edit().putInt(klucz, kolorArgb).apply()
        _koloryStatusow.value = wczytajKoloryStatusow()
    }

    override fun obserwujKonfiguracjeSerwera(): StateFlow<KonfiguracjaSerweraBazy> =
        _konfiguracjaSerwera.asStateFlow()

    override suspend fun ustawAdresSerwera(wartosc: String) = withContext(Dispatchers.IO) {
        preferencje.edit().putString(KLUCZ_SERWER_ADRES, wartosc).apply()
        _konfiguracjaSerwera.value = _konfiguracjaSerwera.value.copy(adres = wartosc)
    }

    override suspend fun ustawPortSerwera(wartosc: String) = withContext(Dispatchers.IO) {
        preferencje.edit().putString(KLUCZ_SERWER_PORT, wartosc).apply()
        _konfiguracjaSerwera.value = _konfiguracjaSerwera.value.copy(port = wartosc)
    }

    override suspend fun ustawNazweUzytkownikaSerwera(wartosc: String) = withContext(Dispatchers.IO) {
        preferencje.edit().putString(KLUCZ_SERWER_NAZWA_UZYTKOWNIKA, wartosc).apply()
        _konfiguracjaSerwera.value = _konfiguracjaSerwera.value.copy(nazwaUzytkownika = wartosc)
    }

    override suspend fun ustawHasloSerwera(wartosc: String) = withContext(Dispatchers.IO) {
        preferencje.edit().putString(KLUCZ_SERWER_HASLO, wartosc).apply()
        _konfiguracjaSerwera.value = _konfiguracjaSerwera.value.copy(haslo = wartosc)
    }

    private fun wczytajKonfiguracjeSerwera(): KonfiguracjaSerweraBazy = KonfiguracjaSerweraBazy(
        adres = preferencje.getString(KLUCZ_SERWER_ADRES, "").orEmpty(),
        port = preferencje.getString(KLUCZ_SERWER_PORT, "").orEmpty(),
        nazwaUzytkownika = preferencje.getString(KLUCZ_SERWER_NAZWA_UZYTKOWNIKA, "").orEmpty(),
        haslo = preferencje.getString(KLUCZ_SERWER_HASLO, "").orEmpty()
    )

    private fun wczytajTrybMotywu(): TrybMotywu {
        val nazwa = preferencje.getString(KLUCZ_TRYB_MOTYWU, null) ?: return TrybMotywu.SYSTEMOWY
        return runCatching { TrybMotywu.valueOf(nazwa) }.getOrDefault(TrybMotywu.SYSTEMOWY)
    }

    private fun wczytajKoloryStatusow(): KoloryStatusow = KoloryStatusow(
        przeterminowane = preferencje.getInt(KLUCZ_KOLOR_PRZETERMINOWANE, DOMYSLNE_KOLORY_STATUSOW.przeterminowane),
        pilne = preferencje.getInt(KLUCZ_KOLOR_PILNE, DOMYSLNE_KOLORY_STATUSOW.pilne),
        wkrotce = preferencje.getInt(KLUCZ_KOLOR_WKROTCE, DOMYSLNE_KOLORY_STATUSOW.wkrotce)
    )

    private fun kluczKoloru(status: StatusKolorowy): String = when (status) {
        StatusKolorowy.PRZETERMINOWANE -> KLUCZ_KOLOR_PRZETERMINOWANE
        StatusKolorowy.PILNE -> KLUCZ_KOLOR_PILNE
        StatusKolorowy.WKROTCE -> KLUCZ_KOLOR_WKROTCE
    }
}
