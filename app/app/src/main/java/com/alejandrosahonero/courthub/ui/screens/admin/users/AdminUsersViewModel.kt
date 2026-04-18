package com.alejandrosahonero.courthub.ui.screens.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.data.local.mapper.toDomain
import com.alejandrosahonero.courthub.data.model.firestore.UserDto
import com.alejandrosahonero.courthub.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminUsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val error: String? = null
)

class AdminUsersViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("role", "client")
                    .get().await()
                val users = snapshot.documents.mapNotNull { doc ->
                    val dto = doc.toObject(UserDto::class.java) ?: return@mapNotNull null
                    dto.toDomain(doc.id)
                }
                _uiState.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun filteredUsers(): List<User> {
        val q = _uiState.value.searchQuery
        return _uiState.value.users.filter {
            q.isBlank() ||
                    it.name.contains(q, ignoreCase = true) ||
                    it.email.contains(q, ignoreCase = true)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    companion object {
        fun factory(firestore: FirebaseFirestore) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminUsersViewModel(firestore) as T
            }
    }
}