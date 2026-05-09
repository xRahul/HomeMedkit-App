package `in`.rahulja.medicinekit.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.activity.ComponentActivity
import `in`.rahulja.medicinekit.utils.AppPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

object AppLocale : KoinComponent {

    private val preferences: AppPreferences by inject()

    fun wrapContext(context: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context
        }

        val locale = preferences.language ?: return context

        val newLocale = Locale.forLanguageTag(locale)
        Locale.setDefault(newLocale)

        val configuration = context.resources.configuration
        configuration.setLocales(LocaleList(newLocale))

        return context.createConfigurationContext(configuration)
    }

    fun setLocale(activity: ComponentActivity, locale: String) {
        preferences.setLanguage(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = activity.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(locale)
        } else {
            activity.recreate()
        }
    }
}