package pt.ua.icm.refinder.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleMaintenanceWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<MaintenanceWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "refinder_maintenance_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // Para testar sem esperar 15 minutos
    fun runMaintenanceNow(context: Context) {
        val request = androidx.work.OneTimeWorkRequestBuilder<MaintenanceWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
