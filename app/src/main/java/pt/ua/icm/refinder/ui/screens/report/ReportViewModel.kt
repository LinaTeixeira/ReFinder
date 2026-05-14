package pt.ua.icm.refinder.ui.screens.report

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository
import android.net.Uri

class ReportViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()

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

        // 1. Converter o Bitmap para o formato que o ML Kit entende
        val image = InputImage.fromBitmap(bitmap, 0)

        // 2. Usar o modelo base gratuito do ML Kit
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        // 3. Processar a imagem
        labeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isEmpty()) {
                    onResult("Não foi possível identificar o objeto na imagem.")
                } else {
                    // Pega nas 4 tags mais confiáveis (com maior índice de certeza) e junta-as
                    // O ML Kit devolve as tags em inglês (ex: "Footwear", "Mobile phone")
                    val tags = labels.take(4).joinToString(", ") { it.text }
                    onResult(tags)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                errorMessage = "Erro a analisar imagem: ${e.localizedMessage}"
            }
            .addOnCompleteListener {
                // Executa sempre no final, quer tenha sucesso ou erro
                isAiLoading = false
            }
    }

    fun submitItem(
        title: String,
        description: String,
        type: String,
        category: String,
        locationName: String,
        date: String,
        latitude: Double?,
        longitude: Double?,
        imageUri: Uri?
    ) {
        isLoading = true
        successMessage = null
        errorMessage = null

        repository.saveItem(
            title = title,
            description = description,
            type = type,
            category = category,
            locationName = locationName,
            date = date,
            latitude = latitude,
            longitude = longitude,
            imageUri = imageUri,
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