package pt.ua.icm.refinder.ui.screens.home


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class HomeViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var items by mutableStateOf<List<LostItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        observeItems()
    }

    private fun observeItems() {
        isLoading = true
        errorMessage = null
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        listenerRegistration = repository.observeItems(
            onDataChanged = { loadedItems ->
                items = loadedItems.filter { it.userId != currentUserId && it.status != "claimed" }
                isLoading = false
            },
            onError = { exception ->
                errorMessage = exception.message ?: "Erro ao carregar itens."
                isLoading = false
            }
        )
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}