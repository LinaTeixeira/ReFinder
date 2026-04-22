package pt.ua.icm.refinder.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository

class SearchViewModel : ViewModel() {

    private val repository = FirebaseItemRepository()
    private var listenerRegistration: ListenerRegistration? = null

    // Lista original vinda do Firestore
    private var allItems = listOf<LostItem>()

    // Estados da UI
    var searchQuery by mutableStateOf("")
        private set

    var selectedType by mutableStateOf("lost") // "lost" ou "found"
        private set

    // Fluxo da lista filtrada
    private val _filteredItems = MutableStateFlow<List<LostItem>>(emptyList())
    val filteredItems: StateFlow<List<LostItem>> = _filteredItems

    init {
        observeItems()
    }

    private fun observeItems() {
        listenerRegistration = repository.observeItems(
            onDataChanged = { items ->
                allItems = items
                applyFilters()
            },
            onError = {
                // Handle error if needed
            }
        )
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        applyFilters()
    }

    fun onTypeChange(type: String) {
        selectedType = type
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = allItems.filter { item ->
            val matchesText = item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            val matchesType = item.type.lowercase() == selectedType.lowercase()

            matchesText && matchesType
        }
        _filteredItems.value = filtered
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
