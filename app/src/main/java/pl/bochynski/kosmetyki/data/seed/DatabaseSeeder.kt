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

            val elementySeeda = wczytajSeed()
            val produkty = elementySeeda.mapNotNull { element ->
                val nazwaKategorii = MAPOWANIE_KATEGORII_SEEDA[element.kategoria] ?: element.kategoria
                val kategoriaId = idKategoriiPoNazwie[nazwaKategorii]
                if (kategoriaId == null) {
                    Log.w(TAG, "Pominięto produkt \"${element.kosmetyk}\" — nieznana kategoria: ${element.kategoria}")
                    return@mapNotNull null
                }
                element.doEncji(kategoriaId)
            }
            produktyDao.wstawWszystkie(produkty)
            Log.i(TAG, "Zaseedowano ${idKategoriiPoNazwie.size} kategorii i ${produkty.size} produktów")
        }
    }

    private fun wczytajSeed(): List<SeedProdukt> {
        val tekst = context.assets.open("seed.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val tablica = JSONArray(tekst)
        return (0 until tablica.length()).map { indeks -> SeedProdukt.zJson(tablica.getJSONObject(indeks)) }
    }
}
