package `in`.rahulja.medicinekit.utils.enums

import androidx.annotation.StringRes
import `in`.rahulja.medicinekit.R

enum class AiMode(@StringRes val title: Int) {
    ML_KIT(R.string.preference_ai_mode_mlkit),
    GEMINI(R.string.preference_ai_mode_gemini)
}
