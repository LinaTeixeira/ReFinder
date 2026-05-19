package pt.ua.icm.refinder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class MaintenanceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = Firebase.firestore

    override suspend fun doWork(): Result {
        return try {
            cleanupReadOldNotifications()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun cleanupReadOldNotifications() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)

        val oldReadNotifications = db.collection("notifications")
            .whereEqualTo("isRead", true)
            .whereLessThan("createdAt", thirtyDaysAgo)
            .get()
            .await()

        if (oldReadNotifications.isEmpty) return

        val batch = db.batch()

        oldReadNotifications.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        batch.commit().await()
    }
}
