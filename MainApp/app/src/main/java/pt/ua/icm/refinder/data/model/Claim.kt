package pt.ua.icm.refinder.data.model

data class Claim(
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemOwnerId: String = "",
    val claimantUserId: String = "",
    val claimantEmail: String = "",
    val message: String = "",
    val status: String = "pending_admin", // pending_admin, approved, rejected
    val createdAt: Long = System.currentTimeMillis()
)
