package com.example.mammothnative.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mammothnative.core.ApiException
import com.example.mammothnative.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PHONE_PREFIX = "010"
private const val PHONE_LENGTH = 11
private const val CODE_LENGTH = 6
private const val CODE_EXPIRE_SECOND = 60
private const val RESEND_COOLDOWN_SECOND = 60

data class PhoneSignInUiState(
    /** 숫자만 담는다. 항상 010 으로 시작한다. */
    val phoneNo: String = PHONE_PREFIX,
    val code: String = "",
    val isSmsSent: Boolean = false,
    val isSending: Boolean = false,
    val isSubmitting: Boolean = false,
    /** 인증번호 유효시간(초) */
    val codeSecondsLeft: Int = 0,
    /** 재발송 가능까지 남은 시간(초) */
    val resendSecondsLeft: Int = 0,
    val errorText: String? = null,
) {
    val formattedPhoneNo: String = formatPhoneNumber(phoneNo)

    val canSendSms: Boolean
        get() = phoneNo.length == PHONE_LENGTH && !isSending && resendSecondsLeft == 0

    val canSubmit: Boolean
        get() = isSmsSent && code.length == CODE_LENGTH && codeSecondsLeft > 0 && !isSubmitting

    /** 웹의 `leftTime` (mm:ss) */
    val codeTimerText: String
        get() = "%02d:%02d".format(codeSecondsLeft / 60, codeSecondsLeft % 60)
}

sealed interface PhoneSignInEvent {
    data object SignedIn : PhoneSignInEvent
    data class Message(val text: String) : PhoneSignInEvent
}

/**
 * 웹 SignInWithPhoneActivity 의 상태(redux signInSlice)와 두 개의 mutation 을 옮긴 것.
 */
@HiltViewModel
class PhoneSignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneSignInUiState())
    val uiState: StateFlow<PhoneSignInUiState> = _uiState.asStateFlow()

    private val _events = Channel<PhoneSignInEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var codeTimerJob: Job? = null
    private var resendTimerJob: Job? = null

    fun onPhoneChange(input: String) {
        _uiState.update { it.copy(phoneNo = toPhoneDigits(input)) }
    }

    fun onCodeChange(input: String) {
        val digits = input.filter(Char::isDigit).take(CODE_LENGTH)
        _uiState.update { it.copy(code = digits, errorText = null) }
    }

    fun sendSms() {
        val state = _uiState.value
        if (!state.canSendSms) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            runCatching { authRepository.sendSms(state.phoneNo) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isSending = false, isSmsSent = true, code = "", errorText = null)
                    }
                    startCodeTimer()
                    startResendCooldown()
                    _events.send(PhoneSignInEvent.Message("인증번호를 보냈어요."))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSending = false) }
                    _events.send(PhoneSignInEvent.Message(error.userMessage()))
                }
        }
    }

    /** 인증번호 확인 → 성공하면 바로 로그인까지 진행한다(웹과 동일한 순서). */
    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val verified = runCatching { authRepository.verifySms(state.phoneNo, state.code) }
            if (verified.isFailure) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorText = "인증번호가 일치하지 않아요. 다시 입력해 주세요.",
                    )
                }
                return@launch
            }

            runCatching { authRepository.signInWithPhone(state.phoneNo) }
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(PhoneSignInEvent.SignedIn)
                }
                .onFailure {
                    // 웹은 여기서 회원가입(본인인증) 화면으로 보낸다. 아직 네이티브에 없는 흐름이다.
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(
                        PhoneSignInEvent.Message("가입되지 않은 번호예요. 회원가입은 아직 앱에 없어요."),
                    )
                }
        }
    }

    private fun startCodeTimer() {
        codeTimerJob?.cancel()
        codeTimerJob = viewModelScope.launch {
            for (second in CODE_EXPIRE_SECOND downTo 0) {
                _uiState.update { it.copy(codeSecondsLeft = second) }
                if (second == 0) {
                    _uiState.update { it.copy(errorText = "입력 시간이 초과되었어요.") }
                    break
                }
                delay(1000)
            }
        }
    }

    private fun startResendCooldown() {
        resendTimerJob?.cancel()
        resendTimerJob = viewModelScope.launch {
            for (second in RESEND_COOLDOWN_SECOND downTo 0) {
                _uiState.update { it.copy(resendSecondsLeft = second) }
                if (second == 0) break
                delay(1000)
            }
        }
    }
}

/** 웹 `toPhoneDigits` — 항상 010 으로 시작하는 11자리로 맞춘다. */
private fun toPhoneDigits(value: String): String {
    val digits = value.filter(Char::isDigit)
    val rest = digits.removePrefix(PHONE_PREFIX)
    return (PHONE_PREFIX + rest).take(PHONE_LENGTH)
}

/** 웹 `formatPhoneNumber` */
private fun formatPhoneNumber(digits: String): String = when {
    digits.length < 4 -> digits
    digits.length < 8 -> "${digits.take(3)}-${digits.drop(3)}"
    else -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
}

private fun Throwable.userMessage(): String = when (this) {
    is ApiException -> message ?: "잠시 후 다시 시도해 주세요."
    else -> "네트워크 상태를 확인해 주세요."
}
