package pt.ua.icm.refinder.data.model

import com.google.firebase.firestore.PropertyName

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info", // info, claim_approved, claim_rejected
    val relatedItemId: String? = null,
    val relatedItemTitle: String = "",
    val relatedItemType: String = "",
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
