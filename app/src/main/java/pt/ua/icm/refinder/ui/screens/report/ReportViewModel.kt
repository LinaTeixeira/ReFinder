package pt.ua.icm.refinder.ui.screens.report

import android.app.Application
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import com.google.mlkit.genai.imagedescription.ImageDescriber
import com.google.mlkit.genai.imagedescription.ImageDescriberOptions
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriptionRequest
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseItemRepository()

    private val describer: ImageDescriber by lazy {
        val options = ImageDescriberOptions.builder(getApplication()).build()
        ImageDescription.getClient(options)
    }

    var isLoading by mutableStateOf(false)
        private set

    var isAiLoading by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun generateAiDescription(bitmap: Bitmap, onResult: (String) -> Unit) {
        isAiLoading = true
        errorMessage = null

        val request = ImageDescriptionRequest.builder(bitmap).build()
        val mainExecutor = ContextCompat.getMainExecutor(getApplication())

        val future = describer.runInference(request)

        Futures.addCallback(
            future,
            object : FutureCallback<com.google.mlkit.genai.imagedescription.ImageDescriptionResult> {
                override fun onSuccess(result: com.google.mlkit.genai.imagedescription.ImageDescriptionResult?) {
                    val text = result?.description?.trim().orEmpty()

                    if (text.isNotBlank()) {
                        onResult(text)
                    } else {
                        onResult("Não foi possível gerar uma descrição para este objeto.")
                    }

                    isAiLoading = false
                }

                override fun onFailure(t: Throwable) {
                    t.printStackTrace()
                    errorMessage = "Erro a processar imagem: ${t.localizedMessage ?: "erro desconhecido"}"
                    isAiLoading = false
                }
            },
            mainExecutor
        )
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

    override fun onCleared() {
        super.onCleared()
        describer.close()
    }
}