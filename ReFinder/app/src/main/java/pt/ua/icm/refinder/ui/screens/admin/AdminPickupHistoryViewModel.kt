package pt.ua.icm.refinder.ui.screens.admin

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class AdminPickupHistoryViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var items by mutableStateOf<List<LostItem>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        listenerRegistration = repository.listenClaimedItems(
            onSuccess = { items = it },
            onFailure = { errorMessage = it.message }
        )
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
