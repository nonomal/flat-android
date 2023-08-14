package io.agora.flat.ui.activity.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.flat.common.android.CallingCodeManager
import io.agora.flat.common.android.StringFetcher
import io.agora.flat.data.model.PhoneOrEmailInfo
import io.agora.flat.data.onFailure
import io.agora.flat.data.onSuccess
import io.agora.flat.data.repository.UserRepository
import io.agora.flat.ui.util.ObservableLoadingCounter
import io.agora.flat.ui.util.UiErrorMessage
import io.agora.flat.ui.util.UiInfoMessage
import io.agora.flat.ui.util.UiMessage
import io.agora.flat.ui.util.UiMessageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val stringFetcher: StringFetcher,
) : ViewModel() {
    private val loading = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()

    private var _state = MutableStateFlow(PasswordResetUiState.Init)
    val state: StateFlow<PasswordResetUiState>
        get() = _state

    init {
        viewModelScope.launch {
            loading.observable.collect {
                _state.value = _state.value.copy(loading = it)
            }
        }

        viewModelScope.launch {
            uiMessageManager.message.collect {
                _state.value = _state.value.copy(message = it)
            }
        }
    }

    private fun sendCodeByPhone(phone: String) {
        viewModelScope.launch {
            loading.addLoader()
            userRepository.requestResetPhoneCode(phone)
                .onSuccess {
                    showUiInfo(stringFetcher.codeSendSuccess())
                }
                .onFailure {
                    showUiError(it)
                }
            loading.removeLoader()
        }
    }

    private fun sendCodeByEmail(email: String) {
        viewModelScope.launch {
            loading.addLoader()
            userRepository.requestResetEmailCode(email)
                .onSuccess {
                    showUiInfo(stringFetcher.codeSendSuccess())
                }
                .onFailure {
                    showUiError(it)
                }
            loading.removeLoader()
        }
    }

    fun sendCode() {
        val info = state.value.info
        if (info.isPhone) {
            sendCodeByPhone(info.phone)
        } else {
            sendCodeByEmail(info.email)
        }
    }

    private fun resetPasswordByPhone(phone: String, password: String, code: String) {
        viewModelScope.launch {
            loading.addLoader()
            userRepository.resetWithPhone(phone = phone, password = password, code = code)
                .onSuccess {
                    _state.value = _state.value.copy(success = true)
                }
                .onFailure {
                    showUiError(it)
                }
            loading.removeLoader()
        }
    }

    private fun resetPasswordByEmail(email: String, password: String, code: String) {
        viewModelScope.launch {
            loading.addLoader()
            userRepository.resetWithEmail(email = email, password = password, code = code)
                .onSuccess {
                    _state.value = _state.value.copy(success = true)
                }
                .onFailure {
                    showUiError(it)
                }
            loading.removeLoader()
        }
    }

    fun changePhoneOrEmailState(state: PhoneOrEmailInfo) {
        _state.value = _state.value.copy(info = state)
    }

    fun nextStep() {
        if (_state.value.step == Step.FetchCode) {
            _state.value = _state.value.copy(step = Step.Confirm)
        }
    }

    fun prevStep() {
        if (_state.value.step == Step.Confirm) {
            _state.value = _state.value.copy(step = Step.FetchCode)
        }
    }

    fun resetPassword() {
        val info = state.value.info
        if (info.isPhone) {
            resetPasswordByPhone(info.phone, info.password, info.code)
        } else {
            resetPasswordByEmail(info.email, info.password, info.code)
        }
    }

    private fun showUiInfo(message: String) {
        viewModelScope.launch {
            uiMessageManager.emitMessage(UiInfoMessage(message))
        }
    }

    private fun showUiError(e: Throwable) {
        viewModelScope.launch {
            uiMessageManager.emitMessage(UiErrorMessage(e))
        }
    }

    fun clearUiMessage(id: Long) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(id)
        }
    }
}

data class PasswordResetUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: UiMessage? = null,

    val step: Step = Step.FetchCode,
    val info: PhoneOrEmailInfo = PhoneOrEmailInfo(
        cc = CallingCodeManager.getDefaultCC()
    ),
) {
    companion object {
        val Init = PasswordResetUiState()
    }
}

enum class Step {
    FetchCode,
    Confirm,
}