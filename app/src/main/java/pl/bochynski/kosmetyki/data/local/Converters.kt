package pl.bochynski.kosmetyki.data.local

import androidx.room.TypeConverter
import pl.bochynski.kosmetyki.data.local.entity.JednostkaOkresuZuzycia
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun zLocalDate(data: LocalDate?): String? = data?.toString()

    @TypeConverter
    fun doLocalDate(wartosc: String?): LocalDate? = wartosc?.let(LocalDate::parse)

    @TypeConverter
    fun zStatusu(status: StatusProduktu): String = status.name

    @TypeConverter
    fun doStatusu(wartosc: String): StatusProduktu = StatusProduktu.valueOf(wartosc)

    @TypeConverter
    fun zJednostki(jednostka: JednostkaOkresuZuzycia): String = jednostka.name

    @TypeConverter
    fun doJednostki(wartosc: String): JednostkaOkresuZuzycia = JednostkaOkresuZuzycia.valueOf(wartosc)
}
