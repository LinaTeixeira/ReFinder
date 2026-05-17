package pt.ua.icm.refinder.ui.screens.admin

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import pt.ua.icm.refinder.data.model.Locker
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class AdminQrValidationViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var lockers by mutableStateOf<List<Locker>>(emptyList())
        private set

    var selectedLockerId by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        listenerRegistration = repository.listenOccupiedLockers(
            onSuccess = { lockers = it },
            onFailure = { errorMessage = it.message }
        )
    }

    fun onLockerChange(lockerId: String) {
        selectedLockerId = lockerId
    }

    fun validateQr(rawQr: String) {
        if (selectedLockerId.isBlank()) {
            errorMessage = "Escolhe primeiro um cacifo."
            return
        }

        val pin = rawQr.removePrefix("REFINDER_PICKUP:").trim()

        if (pin.length != 6 || pin.any { !it.isDigit() }) {
            errorMessage = "QR Code inválido."
            return
        }

        isLoading = true
        successMessage = null
        errorMessage = null

        repository.validatePickup(
            lockerId = selectedLockerId,
            pin = pin,
            onSuccess = {
                isLoading = false
                successMessage = "Levantamento confirmado. O cacifo foi libertado."
                selectedLockerId = ""
            },
            onFailure = {
                isLoading = false
                errorMessage = it.message
            }
        )
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
