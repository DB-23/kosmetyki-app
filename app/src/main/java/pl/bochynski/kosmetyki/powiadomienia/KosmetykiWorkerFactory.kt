package pl.bochynski.kosmetyki.powiadomienia

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository

class KosmetykiWorkerFactory(
    private val produktRepository: ProduktRepository,
    private val ustawieniaRepository: UstawieniaRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        PowiadomieniaWorker::class.java.name ->
            PowiadomieniaWorker(appContext, workerParameters, produktRepository, ustawieniaRepository)
        else -> null
    }
}
