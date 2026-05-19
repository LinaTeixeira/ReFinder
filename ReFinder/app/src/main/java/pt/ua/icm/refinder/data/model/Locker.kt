package pt.ua.icm.refinder.data.model

data class Locker(
    val id: String = "",
    val name: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = true,
    val currentItemId: String? = null
)