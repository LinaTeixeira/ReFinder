package pt.ua.icm.refinder.ui.screens.report

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class ReportViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()

    // Using the Google AI SDK (Works on Spark/Free plan)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyDmnwlQEEHfO7WEALl_MQcgWgSMO8iKx8c"
    )


    var isLoading by mutableStateOf(false)
        private set

    var isAiLoading by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun generateAiDescription(bitmap: Bitmap, onResult: (String) -> Unit) {
        viewModelScope.launch {
            isAiLoading = true
            errorMessage = null
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text("Descreva este objeto para uma aplicação de achados e perdidos. Seja breve e objetivo.")
                    }
                )
                response.text?.let { onResult(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Erro na IA: ${e.localizedMessage}"
            } finally {
                isAiLoading = false
            }
        }
    }

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