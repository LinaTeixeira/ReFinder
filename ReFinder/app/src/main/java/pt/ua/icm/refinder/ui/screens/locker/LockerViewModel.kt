package pt.ua.icm.refinder.ui.screens.locker

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import pt.ua.icm.refinder.data.model.Locker
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class LockerViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()

    var lockers by mutableStateOf<List<Locker>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var generatedPin by mutableStateOf<String?>(null)
        private set

    init {
        repository.listenAvailableLockers(
            onSuccess = { lockers = it },
            onFailure = { errorMessage = it.message }
        )
    }

    fun depositItem(itemId: String, lockerId: String) {
        isLoading = true
        errorMessage = null

        repository.depositItemInLocker(
            itemId = itemId,
            lockerId = lockerId,
            onSuccess = { pin ->
                isLoading = false
                generatedPin = pin
            },
            onFailure = {
                isLoading = false
                errorMessage = it.message
            }
        )
    }
}