package pl.bochynski.kosmetyki.powiadomienia

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import pl.bochynski.kosmetyki.MainActivity
import pl.bochynski.kosmetyki.R
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.data.repository.UstawieniaRepository
import pl.bochynski.kosmetyki.domain.dniDoKonca
import java.util.concurrent.TimeUnit

const val ID_KANALU_POWIADOMIEN = "terminy_produktow"
private const val NAZWA_PRACY_OKRESOWEJ = "sprawdzanie_terminow"
private const val ID_POWIADOMIENIA = 1

class PowiadomieniaWorker(
    context: Context,
    params: WorkerParameters,
    private val produktRepository: ProduktRepository,
    private val ustawieniaRepository: UstawieniaRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val produkty = produktRepository.obserwujWszystkieProdukty().first()
            .filter { it.status != StatusProduktu.ZUZYTE }
        val prog = ustawieniaRepository.pobierzProgPowiadomienDni()

        val liczbaPrzeterminowanych = produkty.count { produkt ->
            val dni = produkt.dniDoKonca()
            dni != null && dni <= 0
        }
        val liczbaWTerminie = produkty.count { produkt ->
            val dni = produkt.dniDoKonca()
            dni != null && dni in 1..prog
        }

        if (liczbaPrzeterminowanych == 0 && liczbaWTerminie == 0) {
            return Result.success()
        }

        pokazPowiadomienie(liczbaPrzeterminowanych, liczbaWTerminie, prog)
        return Result.success()
    }

    private fun pokazPowiadomienie(liczbaPrzeterminowanych: Int, liczbaWTerminie: Int, prog: Int) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val fragmenty = buildList {
            if (liczbaPrzeterminowanych > 0) add("$liczbaPrzeterminowanych przeterminowanych")
            if (liczbaWTerminie > 0) add("$liczbaWTerminie z terminem w ciągu $prog dni")
        }
        val tresc = fragmenty.joinToString(", ").replaceFirstChar { it.uppercase() }

        val intencja = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intencja,
            PendingIntent.FLAG_IMMUTABLE
        )

        val powiadomienie = NotificationCompat.Builder(context, ID_KANALU_POWIADOMIEN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Sprawdź swoje kosmetyki")
            .setContentText(tresc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(ID_POWIADOMIENIA, powiadomienie)
    }

    companion object {
        fun zaplanuj(context: Context) {
            val zadanie = PeriodicWorkRequestBuilder<PowiadomieniaWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NAZWA_PRACY_OKRESOWEJ,
                ExistingPeriodicWorkPolicy.KEEP,
                zadanie
            )
        }
    }
}
