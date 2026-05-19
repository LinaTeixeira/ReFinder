package pt.ua.icm.refinder.ui.screens.notifications

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import pt.ua.icm.refinder.data.model.AppNotification
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class NotificationsViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var notifications by mutableStateOf<List<AppNotification>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        listenerRegistration = repository.listenUserNotifications(
            onSuccess = { notifications = it },
            onFailure = { errorMessage = it.message }
        )
    }

    fun markAsRead(notificationId: String) {
        repository.markNotificationAsRead(
            notificationId = notificationId,
            onFailure = {
                errorMessage = it.message
            }
        )
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
