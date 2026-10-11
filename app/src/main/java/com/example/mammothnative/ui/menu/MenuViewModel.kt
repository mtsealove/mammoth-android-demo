package com.example.mammothnative.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.data.repository.AuthRepository
import com.example.mammothnative.data.repository.UserRepository
import com.example.mammothnative.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val isSignedIn: Boolean = false,
    val profile: Loadable<UserProfile> = Loadable.Loading,
)

/** 웹 MenuFragment 의 사용자 영역만 먼저 옮긴 것. */
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isSignedIn.distinctUntilChanged().collect { signedIn ->
                _uiState.update { it.copy(isSignedIn = signedIn) }
                if (signedIn) loadProfile() else _uiState.update { it.copy(profile = Loadable.Loading) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = Loadable.Loading) }
            val result = runCatching { userRepository.getProfile() }
            _uiState.update {
                it.copy(
                    profile = result.fold(
                        onSuccess = { profile -> Loadable.Success(profile) },
                        onFailure = { error -> Loadable.Failure(error) },
                    ),
                )
            }
        }
    }
}
