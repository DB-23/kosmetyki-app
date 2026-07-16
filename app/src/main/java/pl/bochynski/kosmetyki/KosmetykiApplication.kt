package pl.bochynski.kosmetyki

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import pl.bochynski.kosmetyki.data.local.AppDatabase
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.KategoriaRepositoryImpl
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepositoryImpl

class KosmetykiApplication : Application() {
    private val zasiegAplikacji = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val baza: AppDatabase by lazy { AppDatabase.pobierzInstancje(this, zasiegAplikacji) }

    val kategoriaRepository: KategoriaRepository by lazy { KategoriaRepositoryImpl(baza.kategoriaDao()) }
    val produktRepository: ProduktRepository by lazy { ProduktRepositoryImpl(baza.produktDao()) }
}
