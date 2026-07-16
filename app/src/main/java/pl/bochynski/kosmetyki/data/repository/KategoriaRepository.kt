package pl.bochynski.kosmetyki.data.repository

import kotlinx.coroutines.flow.Flow
import pl.bochynski.kosmetyki.data.local.dao.KategoriaDao
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity

interface KategoriaRepository {
    fun obserwujKategorie(): Flow<List<KategoriaEntity>>
}

class KategoriaRepositoryImpl(
    private val dao: KategoriaDao
) : KategoriaRepository {
    override fun obserwujKategorie(): Flow<List<KategoriaEntity>> = dao.obserwujWszystkie()
}
