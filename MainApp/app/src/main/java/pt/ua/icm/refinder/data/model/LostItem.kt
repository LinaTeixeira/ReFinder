package pt.ua.icm.refinder.data.model

data class LostItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "lost", // "lost" ou "found"
    val category: String = "Outro",
    val tags: List<String> = emptyList(),
    val date: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl : String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    
    val lockerId: String? = null,
    val pickupPin: String? = null,
    val claimedByUserId: String? = null,
    val claimedByUserEmail: String? = null,
    val pickedUpByUserId: String? = null,
    val pickedUpByUserEmail: String? = null,
    val pickedUpAt: Long? = null,
    val status: String = "reported" // reported, deposited, ready_for_pickup, claimed
)