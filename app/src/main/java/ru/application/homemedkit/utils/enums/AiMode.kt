package ru.application.homemedkit.utils.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class AiMode(@StringRes val title: Int) {
    ML_KIT(R.string.preference_ai_mode_mlkit),
    GEMINI(R.string.preference_ai_mode_gemini)
}
