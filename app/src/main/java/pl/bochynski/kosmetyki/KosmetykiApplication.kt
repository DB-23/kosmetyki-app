package pl.bochynski.kosmetyki

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Configuration
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import pl.bochynski.kosmetyki.data.local.AppDatabase
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.KategoriaRepositoryImpl
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepositoryImpl
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepositoryImpl
import pl.bochynski.kosmetyki.data.seed.DatabaseSeeder
import pl.bochynski.kosmetyki.powiadomienia.ID_KANALU_POWIADOMIEN
import pl.bochynski.kosmetyki.powiadomienia.KosmetykiWorkerFactory
import pl.bochynski.kosmetyki.powiadomienia.PowiadomieniaWorker

class KosmetykiApplication : Application(), Configuration.Provider {
    private val zasiegAplikacji = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val baza: AppDatabase by lazy { AppDatabase.pobierzInstancje(this, zasiegAplikacji) }

    val kategoriaRepository: KategoriaRepository by lazy {
        KategoriaRepositoryImpl(baza.kategoriaDao(), baza.produktDao())
    }
    val produktRepository: ProduktRepository by lazy { ProduktRepositoryImpl(baza.produktDao()) }
    val ustawieniaRepository: UstawieniaRepository by lazy { UstawieniaRepositoryImpl(this) }
    val databaseSeeder: DatabaseSeeder by lazy { DatabaseSeeder(this, baza) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(KosmetykiWorkerFactory(produktRepository, ustawieniaRepository))
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
        utworzKanalPowiadomien()
        PowiadomieniaWorker.zaplanuj(this)
    }

    private fun utworzKanalPowiadomien() {
        val kanal = NotificationChannel(
            ID_KANALU_POWIADOMIEN,
            "Terminy produktów",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Powiadomienia o kosmetykach, których termin się zbliża lub minął"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(kanal)
    }
}
