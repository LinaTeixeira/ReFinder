package pt.ua.icm.refinder.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.model.Locker
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
                .addOnSuccessListener { onSuccess() }
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
}