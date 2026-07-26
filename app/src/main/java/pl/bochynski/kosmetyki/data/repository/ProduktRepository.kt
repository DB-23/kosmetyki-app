package pl.bochynski.kosmetyki.data.repository

import kotlinx.coroutines.flow.Flow
import pl.bochynski.kosmetyki.data.local.dao.ProduktDao
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu

interface ProduktRepository {
    fun obserwujWszystkieProdukty(): Flow<List<ProduktEntity>>
    fun obserwujProduktyWgKategorii(kategoriaId: Long): Flow<List<ProduktEntity>>
    fun obserwujProduktyWgStatusu(status: StatusProduktu): Flow<List<ProduktEntity>>
    fun obserwujProduktPoId(id: Long): Flow<ProduktEntity?>
    fun obserwujMiejscaZakupu(): Flow<List<String>>
    fun obserwujMarki(): Flow<List<String>>
    fun obserwujNazwy(): Flow<List<String>>
    suspend fun znajdzWszystkiePoNazwie(nazwa: String): List<ProduktEntity>
    suspend fun dodaj(produkt: ProduktEntity): Long
    suspend fun dodajWiele(produkty: List<ProduktEntity>)
    suspend fun aktualizuj(produkt: ProduktEntity)
    suspend fun usun(produkt: ProduktEntity)
}

class ProduktRepositoryImpl(
    private val dao: ProduktDao
) : ProduktRepository {
    override fun obserwujWszystkieProdukty(): Flow<List<ProduktEntity>> = dao.obserwujWszystkie()

    override fun obserwujProduktyWgKategorii(kategoriaId: Long): Flow<List<ProduktEntity>> =
        dao.obserwujWgKategorii(kategoriaId)

    override fun obserwujProduktyWgStatusu(status: StatusProduktu): Flow<List<ProduktEntity>> =
        dao.obserwujWgStatusu(status)

    override fun obserwujProduktPoId(id: Long): Flow<ProduktEntity?> = dao.obserwujPoId(id)

    override fun obserwujMiejscaZakupu(): Flow<List<String>> = dao.obserwujMiejscaZakupu()

    override fun obserwujMarki(): Flow<List<String>> = dao.obserwujMarki()

    override fun obserwujNazwy(): Flow<List<String>> = dao.obserwujNazwy()

    override suspend fun znajdzWszystkiePoNazwie(nazwa: String): List<ProduktEntity> =
        dao.znajdzWszystkiePoNazwie(nazwa)

    override suspend fun dodaj(produkt: ProduktEntity): Long = dao.wstaw(produkt)

    override suspend fun dodajWiele(produkty: List<ProduktEntity>) = dao.wstawWszystkie(produkty)

    override suspend fun aktualizuj(produkt: ProduktEntity) = dao.aktualizuj(produkt)

    override suspend fun usun(produkt: ProduktEntity) = dao.usun(produkt)
}
