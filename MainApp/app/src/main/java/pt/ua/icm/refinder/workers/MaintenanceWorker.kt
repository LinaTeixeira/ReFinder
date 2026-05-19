package pt.ua.icm.refinder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import pt.ua.icm.refinder.data.model.AppNotification
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.model.findPossibleMatches

class MaintenanceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = Firebase.firestore

    override suspend fun doWork(): Result {
        return try {
            recheckMatches()
            cleanupOldNotifications()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun recheckMatches() {
        val snapshot = db.collection("items").get().await()

        val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(LostItem::class.java)
        }.filter {
            it.status != "claimed"
        }

        val lostItems = items.filter { it.type == "lost" }

        lostItems.forEach { lostItem ->
            val matches = findPossibleMatches(
                currentItem = lostItem,
                allItems = items
            )

            matches.forEach { match ->
                val foundItem = match.item

                if (lostItem.userId.isBlank()) return@forEach
                if (lostItem.userId == foundItem.userId) return@forEach

                createMatchNotificationIfNeeded(
                    lostItem = lostItem,
                    foundItem = foundItem,
                    score = match.score
                )
            }
        }
    }

    private suspend fun createMatchNotificationIfNeeded(
        lostItem: LostItem,
        foundItem: LostItem,
        score: Int
    ) {
        val existing = db.collection("notifications")
            .whereEqualTo("userId", lostItem.userId)
            .whereEqualTo("type", "match_found")
            .whereEqualTo("relatedItemId", foundItem.id)
            .get()
            .await()

        if (!existing.isEmpty) return

        val notificationRef = db.collection("notifications").document()

        val notification = AppNotification(
            id = notificationRef.id,
            userId = lostItem.userId,
            title = "Possível correspondência encontrada",
            message = "Encontrámos '${foundItem.title}', que pode corresponder ao teu anúncio perdido '${lostItem.title}' ($score% de compatibilidade).",
            type = "match_found",
            relatedItemId = foundItem.id,
            relatedItemTitle = foundItem.title,
            relatedItemType = foundItem.type
        )

        notificationRef.set(notification).await()
    }

    private suspend fun cleanupOldNotifications() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)

        val oldNotifications = db.collection("notifications")
            .whereLessThan("createdAt", thirtyDaysAgo)
            .get()
            .await()

        val batch = db.batch()

        oldNotifications.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        if (!oldNotifications.isEmpty) {
            batch.commit().await()
        }
    }
}
