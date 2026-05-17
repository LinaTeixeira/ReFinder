package pt.ua.icm.refinder.data.model

data class ItemMatch(
    val item: LostItem,
    val score: Int,
    val reason: String
)
