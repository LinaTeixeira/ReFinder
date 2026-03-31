package pt.ua.icm.refinder.data.model

data class LostItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "lost", // "lost" ou "found"
    val tags: List<String> = emptyList(),
    val date: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)