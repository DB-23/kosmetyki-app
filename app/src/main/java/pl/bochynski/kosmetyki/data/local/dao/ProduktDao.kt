package pl.bochynski.kosmetyki.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu

@Dao
interface ProduktDao {
    @Query("SELECT * FROM produkty")
    fun obserwujWszystkie(): Flow<List<ProduktEntity>>

    @Query("SELECT * FROM produkty WHERE kategoriaId = :kategoriaId")
    fun obserwujWgKategorii(kategoriaId: Long): Flow<List<ProduktEntity>>

    @Query("SELECT * FROM produkty WHERE status = :status")
    fun obserwujWgStatusu(status: StatusProduktu): Flow<List<ProduktEntity>>

    @Query("SELECT * FROM produkty WHERE id = :id")
    fun obserwujPoId(id: Long): Flow<ProduktEntity?>

    @Insert
    suspend fun wstaw(produkt: ProduktEntity): Long

    @Insert
    suspend fun wstawWszystkie(produkty: List<ProduktEntity>)

    @Update
    suspend fun aktualizuj(produkt: ProduktEntity)

    @Delete
    suspend fun usun(produkt: ProduktEntity)

    @Query("SELECT COUNT(*) FROM produkty")
    suspend fun liczbaProduktow(): Int

    @Query(
        "SELECT DISTINCT miejsceZakupu FROM produkty " +
            "WHERE miejsceZakupu IS NOT NULL ORDER BY miejsceZakupu"
    )
    fun obserwujMiejscaZakupu(): Flow<List<String>>

    @Query("SELECT DISTINCT marka FROM produkty ORDER BY marka")
    fun obserwujMarki(): Flow<List<String>>
}
