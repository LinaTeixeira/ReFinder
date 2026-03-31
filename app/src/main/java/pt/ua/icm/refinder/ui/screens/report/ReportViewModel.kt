package pt.ua.icm.refinder.ui.screens.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class ReportViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()

    var isLoading by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun submitItem(
        title: String,
        description: String,
        type: String,
        locationName: String,
        date: String,
        latitude: Double?,
        longitude: Double?
    ) {
        isLoading = true
        successMessage = null
        errorMessage = null

        repository.saveItem(
            title = title,
            description = description,
            type = type,
            locationName = locationName,
            date = date,
            latitude = latitude,
            longitude = longitude,
            onSuccess = {
                isLoading = false
                successMessage = "Item registado com sucesso."
            },
            onFailure = { exception ->
                isLoading = false
                errorMessage = exception.message ?: "Erro desconhecido."
            }
        )
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }
}