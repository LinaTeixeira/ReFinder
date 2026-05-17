package pt.ua.icm.refinder.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var userEmail by mutableStateOf(auth.currentUser?.email ?: "")
        private set

    var userItems by mutableStateOf<List<LostItem>>(emptyList())
        private set

    var totalItems by mutableStateOf(0)
        private set

    var lostItemsCount by mutableStateOf(0)
        private set

    var foundItemsCount by mutableStateOf(0)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var unreadNotificationsCount by mutableStateOf(0)
        private set

    init {
        observeUserItems()
        checkAdminStatus()
        observeNotifications()
    }

    private fun observeNotifications() {
        repository.listenUserNotifications(
            onSuccess = { notifications ->
                unreadNotificationsCount = notifications.count { !it.isRead }
            },
            onFailure = {}
        )
    }

    private fun checkAdminStatus() {
        repository.isCurrentUserAdmin { result ->
            isAdmin = result
        }
    }

    private fun observeUserItems() {
        val currentUserId = auth.currentUser?.uid ?: return

        listenerRegistration = repository.observeItems(
            onDataChanged = { allItems ->
                val filtered = allItems.filter { it.userId == currentUserId }
                userItems = filtered
                totalItems = filtered.size
                lostItemsCount = filtered.count { it.type == "lost" }
                foundItemsCount = filtered.count { it.type == "found" }
            },
            onError = {
                // Erro pode ser gerido aqui se necessário
            }
        )
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        onLogoutSuccess()
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
