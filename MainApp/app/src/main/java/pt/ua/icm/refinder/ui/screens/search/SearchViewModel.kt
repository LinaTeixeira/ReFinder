package pt.ua.icm.refinder.ui.screens.search

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class SearchViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()

    var allItems by mutableStateOf<List<LostItem>>(emptyList())
        private set

    var searchText by mutableStateOf("")
        private set

    var selectedType by mutableStateOf("all")
        private set

    var selectedCategory by mutableStateOf("Todas")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        repository.listenItems(
            onSuccess = {
                allItems = it.filter { item -> item.userId != currentUserId }
            },
            onFailure = {
                errorMessage = it.message
            }
        )
    }

    val filteredItems: List<LostItem>
        get() {
            return allItems.filter { item ->

                val matchesText =
                    searchText.isBlank() ||
                        item.title.contains(searchText, ignoreCase = true) ||
                        item.description.contains(searchText, ignoreCase = true) ||
                        item.locationName.contains(searchText, ignoreCase = true)

                val matchesType =
                    selectedType == "all" || item.type == selectedType

                val matchesCategory =
                    selectedCategory == "Todas" || item.category == selectedCategory

                matchesText && matchesType && matchesCategory
            }
        }

    fun onSearchTextChange(value: String) {
        searchText = value
    }

    fun onTypeChange(value: String) {
        selectedType = value
    }

    fun onCategoryChange(value: String) {
        selectedCategory = value
    }
}
