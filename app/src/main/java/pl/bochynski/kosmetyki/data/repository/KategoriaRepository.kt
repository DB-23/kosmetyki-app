package pl.bochynski.kosmetyki.data.repository

import kotlinx.coroutines.flow.Flow
import pl.bochynski.kosmetyki.data.local.dao.KategoriaDao
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity

interface KategoriaRepository {
    fun obserwujKategorie(): Flow<List<KategoriaEntity>>
    /** Zwraca false, jesli kategoria o takiej nazwie juz istnieje (bez rozroznienia wielkosci liter). */
    suspend fun dodaj(nazwa: String): Boolean
}

class KategoriaRepositoryImpl(
    private val dao: KategoriaDao
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
}
