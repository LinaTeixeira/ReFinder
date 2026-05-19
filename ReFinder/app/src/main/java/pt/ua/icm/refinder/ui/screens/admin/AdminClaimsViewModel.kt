package pt.ua.icm.refinder.ui.screens.admin

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import pt.ua.icm.refinder.data.model.Claim
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class AdminClaimsViewModel : ViewModel() {
    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var claims by mutableStateOf<List<Claim>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPendingClaims()
    }

    private fun loadPendingClaims() {
        isLoading = true
        listenerRegistration = repository.listenPendingClaims(
            onSuccess = {
                claims = it
                isLoading = false
            },
            onFailure = {
                errorMessage = it.message
                isLoading = false
            }
        )
    }

    fun approveClaim(claimId: String, itemId: String) {
        repository.updateClaimStatus(
            claimId = claimId,
            itemId = itemId,
            newStatus = "approved",
            onSuccess = {
                claims = claims.filterNot { it.id == claimId }
            },
            onFailure = { errorMessage = it.message }
        )
    }

    fun rejectClaim(claimId: String) {
        repository.updateClaimStatus(
            claimId = claimId,
            itemId = null,
            newStatus = "rejected",
            onSuccess = {
                claims = claims.filterNot { it.id == claimId }
            },
            onFailure = { errorMessage = it.message }
        )
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
