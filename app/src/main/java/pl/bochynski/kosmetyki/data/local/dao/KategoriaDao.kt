package pl.bochynski.kosmetyki.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity

@Dao
interface KategoriaDao {
    @Query("SELECT * FROM kategorie ORDER BY kolejnosc ASC")
    fun obserwujWszystkie(): Flow<List<KategoriaEntity>>

    @Query("SELECT * FROM kategorie ORDER BY kolejnosc ASC")
    suspend fun pobierzWszystkie(): List<KategoriaEntity>

    @Insert
    suspend fun wstaw(kategoria: KategoriaEntity): Long

    @Update
    suspend fun aktualizuj(kategoria: KategoriaEntity)

    @Delete
    suspend fun usun(kategoria: KategoriaEntity)

    @Query("SELECT COUNT(*) FROM kategorie")
    suspend fun liczbaKategorii(): Int
}
