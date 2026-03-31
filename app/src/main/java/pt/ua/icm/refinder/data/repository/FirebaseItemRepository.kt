package pt.ua.icm.refinder.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import pt.ua.icm.refinder.data.model.LostItem

class FirebaseItemRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    fun saveItem(
        title: String,
        description: String,
        type: String,
        locationName: String,
        date: String,
        latitude: Double?,
        longitude: Double?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = db.collection("items").document()

        val item = LostItem(
            id = docRef.id,
            title = title,
            description = description,
            type = type,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            date = date,
            userId = auth.currentUser?.uid ?: ""
        )

        docRef.set(item)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e: Exception -> onFailure(e) }
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