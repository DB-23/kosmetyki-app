package pl.bochynski.kosmetyki.data.repository

import kotlinx.coroutines.flow.Flow
import pl.bochynski.kosmetyki.data.local.dao.KategoriaDao
import pl.bochynski.kosmetyki.data.local.dao.ProduktDao
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity

interface KategoriaRepository {
    fun obserwujKategorie(): Flow<List<KategoriaEntity>>
    /** Zwraca false, jesli kategoria o takiej nazwie juz istnieje (bez rozroznienia wielkosci liter). */
    suspend fun dodaj(nazwa: String): Boolean
    /** Zwraca false, jesli inna kategoria o takiej nazwie juz istnieje. */
    suspend fun zmienNazwe(kategoria: KategoriaEntity, nowaNazwa: String): Boolean
    /** Usuwa kategorie, jesli nie jest uzywana przez zaden produkt. Zwraca liczbe produktow blokujacych usuniecie (0 = usunieto). */
    suspend fun usun(kategoria: KategoriaEntity): Int
}

class KategoriaRepositoryImpl(
    private val dao: KategoriaDao,
    private val produktDao: ProduktDao
) : KategoriaRepository {
    override fun obserwujKategorie(): Flow<List<KategoriaEntity>> = dao.obserwujWszystkie()

    override suspend fun dodaj(nazwa: String): Boolean {
        val przycieta = nazwa.trim()
        if (przycieta.isBlank()) return false
        val istniejace = dao.pobierzWszystkie()
        if (istniejace.any { it.nazwa.equals(przycieta, ignoreCase = true) }) return false
        val kolejnosc = (istniejace.maxOfOrNull { it.kolejnosc } ?: -1) + 1
        dao.wstaw(KategoriaEntity(nazwa = przycieta, kolejnosc = kolejnosc))
        return true
    }

    override suspend fun zmienNazwe(kategoria: KategoriaEntity, nowaNazwa: String): Boolean {
        val przycieta = nowaNazwa.trim()
        if (przycieta.isBlank()) return false
        val istniejace = dao.pobierzWszystkie()
        if (istniejace.any { it.id != kategoria.id && it.nazwa.equals(przycieta, ignoreCase = true) }) return false
        dao.aktualizuj(kategoria.copy(nazwa = przycieta))
        return true
    }

    override suspend fun usun(kategoria: KategoriaEntity): Int {
        val liczbaProduktow = produktDao.liczbaWgKategorii(kategoria.id)
        if (liczbaProduktow == 0) {
            dao.usun(kategoria)
        }
        return liczbaProduktow
    }
}
