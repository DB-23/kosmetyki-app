package pl.bochynski.kosmetyki.data.seed

import org.json.JSONObject
import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import java.time.LocalDate

data class SeedProdukt(
    val kategoria: String,
    val marka: String,
    val seria: String?,
    val linia: String?,
    val kosmetyk: String,
    val okresZuzyciaMiesiace: Int?,
    val dataWaznosci: LocalDate?,
    val otwarte: Boolean,
    val dataOtwarcia: LocalDate?
) {
    fun doEncji(kategoriaId: Long): ProduktEntity = ProduktEntity(
        kategoriaId = kategoriaId,
        marka = marka,
        seria = seria,
        linia = linia,
        nazwa = kosmetyk,
        dataWaznosci = dataWaznosci,
        okresZuzyciaPoOtwarciu = okresZuzyciaMiesiace,
        jednostkaOkresuZuzycia = JednostkaOkresuZuzycia.MIESIACE,
        status = if (otwarte) StatusProduktu.OTWARTE else StatusProduktu.W_ZAPASIE,
        dataOtwarcia = dataOtwarcia,
        dataDodania = LocalDate.now()
    )

    companion object {
        fun zJson(json: JSONObject): SeedProdukt = SeedProdukt(
            kategoria = json.getString("kategoria"),
            marka = json.getString("marka"),
            seria = json.optNiepusty("seria"),
            linia = json.optNiepusty("linia"),
            kosmetyk = json.getString("kosmetyk"),
            okresZuzyciaMiesiace = json.optNiepusty("okresZuzyciaMiesiace")?.let(::wyodrebnijLiczbeMiesiecy),
            dataWaznosci = json.optNiepusty("dataWaznosci")?.let(LocalDate::parse),
            otwarte = json.getBoolean("otwarte"),
            dataOtwarcia = json.optNiepusty("dataOtwarcia")?.let(LocalDate::parse)
        )

        private fun wyodrebnijLiczbeMiesiecy(tekst: String): Int? =
            Regex("""\d+""").find(tekst)?.value?.toIntOrNull()

        private fun JSONObject.optNiepusty(klucz: String): String? {
            if (isNull(klucz)) return null
            return optString(klucz).takeIf { it.isNotBlank() }
        }
    }
}
