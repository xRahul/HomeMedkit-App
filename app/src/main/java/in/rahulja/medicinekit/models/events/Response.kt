package `in`.rahulja.medicinekit.models.events

import androidx.annotation.StringRes
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.network.models.MainModel

sealed interface Response {
    data class Success(val model: MainModel) : Response

    sealed class Error(@StringRes val message: Int) : Response {
        data object IncorrectCode : Error(R.string.text_error_not_medicine)
        data object CodeNotFound : Error(R.string.text_code_not_found)
        data object UnknownError : Error(R.string.text_try_again)
        data class NetworkError(val code: String? = null) : Error(R.string.text_connection_error)
    }
}