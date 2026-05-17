package pt.ua.icm.refinder.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.model.Locker
import pt.ua.icm.refinder.data.model.Claim
import pt.ua.icm.refinder.data.model.AppNotification
import pt.ua.icm.refinder.data.model.findPossibleMatches
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlin.random.Random
class FirebaseItemRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = FirebaseStorage.getInstance().reference

    fun saveItem(
        title: String,
        description: String,
        type: String,
        category: String,
        locationName: String,
        date: String,
        latitude: Double?,
        longitude: Double?,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onFailure(Exception("Utilizador não autenticado"))
            return
        }

        val docRef = db.collection("items").document()

        fun saveToFirestore(imageUrl: String) {
            val item = LostItem(
                id = docRef.id,
                title = title,
                description = description,
                type = type,
                category = category,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                date = date,
                imageUrl = imageUrl,
                userId = userId
            )

            docRef.set(item)
                .addOnSuccessListener {
                    notifyPossibleMatchesForNewItem(item)
                    onSuccess()
                }
                .addOnFailureListener { e -> onFailure(e) }
        }

        if (imageUri == null) {
            saveToFirestore("")
            return
        }

        val imageRef = storage.child("items/$userId/${docRef.id}.jpg")

        imageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Erro ao enviar imagem")
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                saveToFirestore(downloadUri.toString())
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun notifyPossibleMatchesForNewItem(newItem: LostItem) {
        db.collection("items")
            .get()
            .addOnSuccessListener { snapshot ->
                val allItems = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(LostItem::class.java)
                }

                val matches = findPossibleMatches(
                    currentItem = newItem,
                    allItems = allItems
                )

                matches.forEach { match ->
                    val lostItem: LostItem
                    val foundItem: LostItem

                    if (newItem.type == "lost") {
                        lostItem = newItem
                        foundItem = match.item
                    } else {
                        lostItem = match.item
                        foundItem = newItem
                    }

                    if (lostItem.userId.isBlank()) return@forEach
                    if (lostItem.userId == foundItem.userId) return@forEach

                    createMatchNotification(
                        lostItem = lostItem,
                        foundItem = foundItem,
                        score = match.score
                    )
                }
            }
    }

    private fun createMatchNotification(
        lostItem: LostItem,
        foundItem: LostItem,
        score: Int
    ) {
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

        notificationRef.set(notification)
    }

    fun getItemById(
        itemId: String,
        onSuccess: (LostItem) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("items")
            .document(itemId)
            .get()
            .addOnSuccessListener { document ->
                val item = document.toObject(LostItem::class.java)
                if (item != null) {
                    onSuccess(item)
                } else {
                    onFailure(Exception("Item não encontrado."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun listenItems(
        onSuccess: (List<LostItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("items")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(LostItem::class.java)
                } ?: emptyList()

                onSuccess(items)
            }
    }

    fun listenAvailableLockers(
        onSuccess: (List<Locker>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("lockers")
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val lockers = snapshot?.documents?.mapNotNull {
                    it.toObject(Locker::class.java)
                } ?: emptyList()

                onSuccess(lockers)
            }
    }

    fun depositItemInLocker(
        itemId: String,
        lockerId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val pin = Random.nextInt(100000, 999999).toString()

        val itemRef = db.collection("items").document(itemId)
        val lockerRef = db.collection("lockers").document(lockerId)

        db.runBatch { batch ->
            batch.update(
                itemRef,
                mapOf(
                    "lockerId" to lockerId,
                    "pickupPin" to pin,
                    "status" to "deposited"
                )
            )

            batch.update(
                lockerRef,
                mapOf(
                    "isAvailable" to false,
                    "currentItemId" to itemId
                )
            )
        }.addOnSuccessListener {
            onSuccess(pin)
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    fun observeItems(
        onDataChanged: (List<LostItem>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(LostItem::class.java)
                } ?: emptyList()

                onDataChanged(items)
            }
    }

    fun createClaim(
        item: LostItem,
        message: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        val currentUserEmail = auth.currentUser?.email ?: "Email indisponível"

        if (currentUserId == null) {
            onFailure(Exception("Utilizador não autenticado"))
            return
        }

        if (currentUserId == item.userId) {
            onFailure(Exception("Não podes reclamar um item que tu próprio registaste."))
            return
        }

        val claimRef = db.collection("claims").document()

        val claim = Claim(
            id = claimRef.id,
            itemId = item.id,
            itemTitle = item.title,
            itemOwnerId = item.userId,
            claimantUserId = currentUserId,
            claimantEmail = currentUserEmail,
            message = message,
            status = "pending_admin"
        )

        claimRef.set(claim)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun isCurrentUserAdmin(onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false)
        db.collection("admins").document(userId).get()
            .addOnSuccessListener { document ->
                onResult(document.exists())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun listenPendingClaims(
        onSuccess: (List<Claim>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("claims")
            .whereEqualTo("status", "pending_admin")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val claims = snapshot?.documents?.mapNotNull { it.toObject(Claim::class.java) } ?: emptyList()
                onSuccess(claims)
            }
    }

    fun updateClaimStatus(
        claimId: String,
        itemId: String?,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val claimRef = db.collection("claims").document(claimId)

        claimRef.get()
            .addOnSuccessListener { snapshot ->
                val claim = snapshot.toObject(Claim::class.java)

                if (claim == null) {
                    onFailure(Exception("Pedido não encontrado."))
                    return@addOnSuccessListener
                }

                val batch = db.batch()

                batch.update(claimRef, "status", newStatus)

                if (newStatus == "approved" && itemId != null) {
                    val itemRef = db.collection("items").document(itemId)
                    batch.update(
                        itemRef,
                        mapOf(
                            "status" to "ready_for_pickup",
                            "claimedByUserId" to claim.claimantUserId
                        )
                    )
                }

                val notificationRef = db.collection("notifications").document()

                val notification = AppNotification(
                    id = notificationRef.id,
                    userId = claim.claimantUserId,
                    title = if (newStatus == "approved") {
                        "Pedido aprovado"
                    } else {
                        "Pedido rejeitado"
                    },
                    message = if (newStatus == "approved") {
                        "O teu pedido sobre '${claim.itemTitle}' foi aprovado. O QR Code de levantamento já está disponível."
                    } else {
                        "O teu pedido sobre '${claim.itemTitle}' foi rejeitado pelo administrador."
                    },
                    type = if (newStatus == "approved") "claim_approved" else "claim_rejected",
                    relatedItemId = claim.itemId,
                    relatedItemTitle = claim.itemTitle,
                    relatedItemType = "found"
                )

                batch.set(notificationRef, notification)

                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun listenUserNotifications(
        onSuccess: (List<AppNotification>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration? {
        val userId = auth.currentUser?.uid ?: return null

        return db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onSuccess(notifications)
            }
    }

    fun markNotificationAsRead(
        notificationId: String,
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnFailureListener { onFailure(it) }
    }
}

