package pl.bochynski.kosmetyki.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity

@Dao
interface KategoriaDao {
    @Query("SELECT * FROM kategorie ORDER BY kolejnosc ASC")
    fun obserwujWszystkie(): Flow<List<KategoriaEntity>>

    @Insert
    suspend fun wstaw(kategoria: KategoriaEntity): Long

    @Query("SELECT COUNT(*) FROM kategorie")
    suspend fun liczbaKategorii(): Int
}
