package io.agora.flat.ui.activity.login

sealed class LoginUiAction {
    object WeChatLogin : LoginUiAction()
    object GithubLogin : LoginUiAction()
    object AgreementHint : LoginUiAction()

    object OpenServiceProtocol : LoginUiAction()
    object OpenPrivacyProtocol : LoginUiAction()

    data class PhoneSendCode(val phone: String) : LoginUiAction()
    data class PhoneLogin(val phone: String, val code: String) : LoginUiAction()
}