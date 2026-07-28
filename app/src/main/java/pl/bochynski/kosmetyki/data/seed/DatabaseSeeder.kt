package pl.bochynski.kosmetyki.data.seed

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import org.json.JSONArray
import pl.bochynski.kosmetyki.data.local.AppDatabase
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity

/**
 * Nazwy startowych kategorii, w kolejności wyświetlania.
 */
val KATEGORIE_STARTOWE = listOf(
    "Dłonie i stopy",
    "Higieniczne/jama ustna i depilacja",
    "Inne",
    "Makijaż",
    "Maseczki i płatki pod oczy",
    "Miniaturki",
    "Paznokcie",
    "Pielęgnacja ciała",
    "Pielęgnacja twarzy",
    "Włosy"
)

/**
 * Nazwy kategorii w seed.json nie zawsze pokrywają się dosłownie z nazwami
 * kategorii startowych (np. "Ciało" w danych źródłowych to "Pielęgnacja ciała").
 */
private val MAPOWANIE_KATEGORII_SEEDA = mapOf(
    "Ciało" to "Pielęgnacja ciała",
    "Włosy" to "Włosy"
)

private const val TAG = "DatabaseSeeder"

class DatabaseSeeder(
    private val context: Context,
    private val database: AppDatabase
) {
    suspend fun zaseeduj() {
        database.withTransaction {
            val kategorieDao = database.kategoriaDao()
            val produktyDao = database.produktDao()

            val idKategoriiPoNazwie = mutableMapOf<String, Long>()
            KATEGORIE_STARTOWE.forEachIndexed { indeks, nazwa ->
                val id = kategorieDao.wstaw(KategoriaEntity(nazwa = nazwa, kolejnosc = indeks))
                idKategoriiPoNazwie[nazwa] = id
            }

            val produkty = zbudujProduktyZSeeda(idKategoriiPoNazwie)
            produktyDao.wstawWszystkie(produkty)
            Log.i(TAG, "Zaseedowano ${idKategoriiPoNazwie.size} kategorii i ${produkty.size} produktów")
        }
    }

    /**
     * Wczytuje przykladowa (demonstracyjna) baze produktow na zadanie uzytkownika
     * (np. po wyzerowaniu bazy). W przeciwienstwie do [zaseeduj] nie zaklada pustej
     * bazy - dopisuje brakujace kategorie startowe (bez duplikowania istniejacych)
     * i dodaje produkty z seed.json do obecnej zawartosci bazy.
     */
    suspend fun wczytajDemo(): Int = database.withTransaction {
        val kategorieDao = database.kategoriaDao()
        val produktyDao = database.produktDao()

        val istniejace = kategorieDao.pobierzWszystkie().associateBy { it.nazwa }
        val idKategoriiPoNazwie = mutableMapOf<String, Long>()
        istniejace.forEach { (nazwa, kategoria) -> idKategoriiPoNazwie[nazwa] = kategoria.id }

        val kolejnoscBazowa = istniejace.values.maxOfOrNull { it.kolejnosc + 1 } ?: 0
        KATEGORIE_STARTOWE.forEachIndexed { indeks, nazwa ->
            if (nazwa !in idKategoriiPoNazwie) {
                val id = kategorieDao.wstaw(KategoriaEntity(nazwa = nazwa, kolejnosc = kolejnoscBazowa + indeks))
                idKategoriiPoNazwie[nazwa] = id
            }
        }

        val produkty = zbudujProduktyZSeeda(idKategoriiPoNazwie)
        produktyDao.wstawWszystkie(produkty)
        Log.i(TAG, "Wczytano baze demonstracyjna: ${produkty.size} produktów")
        produkty.size
    }

    private fun zbudujProduktyZSeeda(idKategoriiPoNazwie: Map<String, Long>) =
        wczytajSeed().mapNotNull { element ->
            val nazwaKategorii = MAPOWANIE_KATEGORII_SEEDA[element.kategoria] ?: element.kategoria
            val kategoriaId = idKategoriiPoNazwie[nazwaKategorii]
            if (kategoriaId == null) {
                Log.w(TAG, "Pominięto produkt \"${element.kosmetyk}\" — nieznana kategoria: ${element.kategoria}")
                return@mapNotNull null
            }
            element.doEncji(kategoriaId)
        }

    private fun wczytajSeed(): List<SeedProdukt> {
        val tekst = context.assets.open("seed.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val tablica = JSONArray(tekst)
        return (0 until tablica.length()).map { indeks -> SeedProdukt.zJson(tablica.getJSONObject(indeks)) }
    }
}
