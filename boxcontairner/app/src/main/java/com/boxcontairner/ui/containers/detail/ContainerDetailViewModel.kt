package com.boxcontairner.ui.containers.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxcontairner.data.repository.AuthRepository
import com.boxcontairner.data.repository.ContainerRepository
import com.boxcontairner.domain.model.Container
import com.boxcontairner.domain.model.Role
import com.boxcontairner.domain.model.StatusHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContainerDetailViewModel @Inject constructor(
    private val repository: ContainerRepository,
    authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val containerId: String = checkNotNull(savedStateHandle["containerId"]) {
        "containerId es obligatorio en la ruta de detalle"
    }
    val startInEditMode: Boolean = savedStateHandle["editMode"] ?: false

    val currentRole: StateFlow<Role> = authRepository.currentRole

    val container: StateFlow<Container?> = repository.getContainer(containerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val history: StateFlow<List<StatusHistory>> = repository.getHistory(containerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveDetail(container: Container) {
        viewModelScope.launch { repository.saveDetail(container) }
    }

    fun updateStatus(previousStatus: String, newStatus: String) {
        viewModelScope.launch { repository.updateStatus(containerId, previousStatus, newStatus) }
    }

    fun deleteOrArchive(onResult: (archived: Boolean) -> Unit) {
        viewModelScope.launch {
            val archived = repository.deleteOrArchive(containerId)
            onResult(archived)
        }
    }
}
