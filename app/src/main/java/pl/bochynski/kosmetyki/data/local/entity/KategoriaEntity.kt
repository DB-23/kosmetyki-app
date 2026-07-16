package pl.bochynski.kosmetyki.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kategorie")
data class KategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nazwa: String,
    val kolejnosc: Int
)
