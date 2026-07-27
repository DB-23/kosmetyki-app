package pl.bochynski.kosmetyki.data.backup

import org.json.JSONArray
import org.json.JSONObject
import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import java.time.LocalDate

private const val WERSJA_EKSPORTU = 1

data class WynikImportu(val produkty: List<ProduktEntity>, val pominieteBrakKategorii: Int)

object KopiaZapasowaProduktow {

    fun zserializuj(produkty: List<ProduktEntity>, kategorie: List<KategoriaEntity>): String {
        val nazwaKategoriiPoId = kategorie.associateBy { it.id }
        val tablica = JSONArray()
        produkty.forEach { produkt ->
            val json = JSONObject()
            json.put("kategoria", nazwaKategoriiPoId[produkt.kategoriaId]?.nazwa.orEmpty())
            json.put("marka", produkt.marka)
            json.put("seria", produkt.seria)
            json.put("linia", produkt.linia)
            json.put("nazwa", produkt.nazwa)
            json.put("ean", produkt.ean)
            json.put("pojemnosc", produkt.pojemnosc)
            json.put("dataWaznosci", produkt.dataWaznosci?.toString())
            json.put("okresZuzyciaPoOtwarciu", produkt.okresZuzyciaPoOtwarciu)
            json.put("jednostkaOkresuZuzycia", produkt.jednostkaOkresuZuzycia.name)
            json.put("status", produkt.status.name)
            json.put("dataOtwarcia", produkt.dataOtwarcia?.toString())
            json.put("dataZuzycia", produkt.dataZuzycia?.toString())
            json.put("dataDodania", produkt.dataDodania.toString())
            json.put("dataZakupu", produkt.dataZakupu?.toString())
            json.put("cenaZakupu", produkt.cenaZakupu)
            json.put("miejsceZakupu", produkt.miejsceZakupu)
            json.put("notatka", produkt.notatka)
            json.put("ulubiony", produkt.ulubiony)
            tablica.put(json)
        }
        val korzen = JSONObject()
        korzen.put("wersjaEksportu", WERSJA_EKSPORTU)
        korzen.put("dataEksportu", LocalDate.now().toString())
        korzen.put("produkty", tablica)
        return korzen.toString(2)
    }

    fun odczytaj(tekst: String, kategorie: List<KategoriaEntity>): WynikImportu {
        val idKategoriiPoNazwie = kategorie.associate { it.nazwa to it.id }
        val korzen = JSONObject(tekst)
        val tablica = korzen.optJSONArray("produkty") ?: JSONArray()
        val produkty = mutableListOf<ProduktEntity>()
        var pominiete = 0
        for (i in 0 until tablica.length()) {
            val json = tablica.getJSONObject(i)
            val kategoriaId = idKategoriiPoNazwie[json.optString("kategoria")]
            if (kategoriaId == null) {
                pominiete++
                continue
            }
            produkty.add(
                ProduktEntity(
                    kategoriaId = kategoriaId,
                    marka = json.optString("marka"),
                    seria = json.optStringOrNull("seria"),
                    linia = json.optStringOrNull("linia"),
                    nazwa = json.optString("nazwa"),
                    ean = json.optStringOrNull("ean"),
                    pojemnosc = json.optStringOrNull("pojemnosc"),
                    dataWaznosci = json.optLocalDateOrNull("dataWaznosci"),
                    okresZuzyciaPoOtwarciu = json.optIntOrNull("okresZuzyciaPoOtwarciu"),
                    jednostkaOkresuZuzycia = runCatching {
                        JednostkaOkresuZuzycia.valueOf(json.optString("jednostkaOkresuZuzycia"))
                    }.getOrDefault(JednostkaOkresuZuzycia.MIESIACE),
                    status = runCatching {
                        StatusProduktu.valueOf(json.optString("status"))
                    }.getOrDefault(StatusProduktu.W_ZAPASIE),
                    dataOtwarcia = json.optLocalDateOrNull("dataOtwarcia"),
                    dataZuzycia = json.optLocalDateOrNull("dataZuzycia"),
                    dataDodania = json.optLocalDateOrNull("dataDodania") ?: LocalDate.now(),
                    dataZakupu = json.optLocalDateOrNull("dataZakupu"),
                    cenaZakupu = if (json.isNull("cenaZakupu")) null else json.optDouble("cenaZakupu"),
                    miejsceZakupu = json.optStringOrNull("miejsceZakupu"),
                    notatka = json.optStringOrNull("notatka"),
                    ulubiony = json.optBoolean("ulubiony", false)
                )
            )
        }
        return WynikImportu(produkty, pominiete)
    }
}

private fun JSONObject.optStringOrNull(klucz: String): String? =
    if (!has(klucz) || isNull(klucz)) null else optString(klucz).takeIf { it.isNotBlank() }

private fun JSONObject.optIntOrNull(klucz: String): Int? =
    if (!has(klucz) || isNull(klucz)) null else optInt(klucz)

private fun JSONObject.optLocalDateOrNull(klucz: String): LocalDate? {
    if (!has(klucz) || isNull(klucz)) return null
    val wartosc = optString(klucz)
    return if (wartosc.isBlank()) null else runCatching { LocalDate.parse(wartosc) }.getOrNull()
}
