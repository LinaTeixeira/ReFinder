package pt.ua.icm.refinder.ui.screens.auth


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import pt.ua.icm.refinder.data.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    var isLoading by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val isLoggedIn: Boolean
        get() = repository.isUserLoggedIn()

    fun login(email: String, password: String) {
        isLoading = true
        successMessage = null
        errorMessage = null

        repository.login(
            email = email,
            password = password,
            onSuccess = {
                isLoading = false
                successMessage = "Login com sucesso."
            },
            onFailure = { e ->
                isLoading = false
                errorMessage = e.message ?: "Erro no login."
            }
        )
    }

    fun register(email: String, password: String) {
        isLoading = true
        successMessage = null
        errorMessage = null

        repository.register(
            email = email,
            password = password,
            onSuccess = {
                isLoading = false
                successMessage = "Conta criada com sucesso."
            },
            onFailure = { e ->
                isLoading = false
                errorMessage = e.message ?: "Erro no registo."
            }
        )
    }

    fun currentUserId(): String {
        return repository.getCurrentUserId()
    }

    fun logout() {
        repository.logout()
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }
}