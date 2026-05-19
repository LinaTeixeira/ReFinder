package pt.ua.icm.refinder.ui.screens.admin

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import pt.ua.icm.refinder.data.model.Locker
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class AdminPickupValidationViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var lockers by mutableStateOf<List<Locker>>(emptyList())
        private set

    var selectedLockerId by mutableStateOf("")
        private set

    var pin by mutableStateOf("")
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

    fun onPinChange(value: String) {
        pin = value.filter { it.isDigit() }.take(6)
    }

    fun validatePickup() {
        if (selectedLockerId.isBlank()) {
            errorMessage = "Escolhe um cacifo."
            return
        }

        if (pin.length != 6) {
            errorMessage = "Introduz um PIN válido de 6 dígitos."
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
                pin = ""
                selectedLockerId = ""
            },
            onFailure = {
                isLoading = false
                errorMessage = it.message
            }
        )
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
