package com.boxcontairner.ui.containers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxcontairner.data.repository.AuthRepository
import com.boxcontairner.data.repository.ContainerRepository
import com.boxcontairner.domain.model.Container
import com.boxcontairner.domain.model.Role
import com.boxcontairner.domain.usecase.RegisterContainerEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContainerListViewModel @Inject constructor(
    private val repository: ContainerRepository,
    private val registerEntry: RegisterContainerEntryUseCase,
    authRepository: AuthRepository
) : ViewModel() {

    val containers: StateFlow<List<Container>> = repository.getContainers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRole: StateFlow<Role> = authRepository.currentRole

    private val _navigateToDetail = MutableSharedFlow<String>()
    val navigateToDetail: SharedFlow<String> = _navigateToDetail

    init {
        // Sync inicial — el ViewModel se crea una vez por entrada a la pantalla.
        viewModelScope.launch { repository.syncContainers() }
    }

    fun scanAndNavigate(container: Container) {
        viewModelScope.launch {
            val id = registerEntry(container)
            _navigateToDetail.emit(id)
        }
    }
}
