package pl.bochynski.kosmetyki.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "produkty",
    foreignKeys = [
        ForeignKey(
            entity = KategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["kategoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("kategoriaId")]
)
data class ProduktEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val kategoriaId: Long,
    val marka: String,
    val seria: String? = null,
    val linia: String? = null,
    val nazwa: String,
    val ean: String? = null,
    val zdjecieUri: String? = null,
    val pojemnosc: String? = null,
    val dataWaznosci: LocalDate? = null,
    val okresZuzyciaPoOtwarciu: Int? = null,
    val jednostkaOkresuZuzycia: JednostkaOkresuZuzycia = JednostkaOkresuZuzycia.MIESIACE,
    val status: StatusProduktu = StatusProduktu.W_ZAPASIE,
    val dataOtwarcia: LocalDate? = null,
    val dataZuzycia: LocalDate? = null,
    val dataDodania: LocalDate = LocalDate.now(),
    val dataZakupu: LocalDate? = null,
    val cenaZakupu: Double? = null,
    val miejsceZakupu: String? = null,
    val notatka: String? = null,
    val ulubiony: Boolean = false
)
