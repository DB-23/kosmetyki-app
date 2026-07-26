package pl.bochynski.kosmetyki.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ProduktZApi(val marka: String?, val nazwa: String?)

object OpenBeautyFactsApi {

    suspend fun pobierzProdukt(ean: String): ProduktZApi? = withContext(Dispatchers.IO) {
        runCatching {
            val polaczenie = URL(
                "https://world.openbeautyfacts.org/api/v2/product/$ean.json"
            ).openConnection() as HttpURLConnection
            polaczenie.requestMethod = "GET"
            polaczenie.connectTimeout = 10_000
            polaczenie.readTimeout = 10_000

            val tekst = polaczenie.inputStream.bufferedReader().use { it.readText() }
            polaczenie.disconnect()

            val json = JSONObject(tekst)
            if (json.optInt("status") != 1) return@runCatching null
            val produkt = json.optJSONObject("product") ?: return@runCatching null

            val marka = produkt.optString("brands")
                .split(",")
                .firstOrNull()
                ?.trim()
                ?.ifBlank { null }
            val nazwa = produkt.optString("product_name").trim().ifBlank { null }

            if (marka == null && nazwa == null) null else ProduktZApi(marka = marka, nazwa = nazwa)
        }.getOrNull()
    }
}
