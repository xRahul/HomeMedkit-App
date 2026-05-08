package `in`.rahulja.medicinekit.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import `in`.rahulja.medicinekit.models.viewModels.AuthViewModel
import `in`.rahulja.medicinekit.models.viewModels.IntakeViewModel
import `in`.rahulja.medicinekit.models.viewModels.MedicineViewModel
import `in`.rahulja.medicinekit.ui.navigation.Screen
import `in`.rahulja.medicinekit.ui.screens.AuthScreen
import `in`.rahulja.medicinekit.ui.screens.IntakeFullScreen
import `in`.rahulja.medicinekit.ui.screens.IntakeScreen
import `in`.rahulja.medicinekit.ui.screens.IntakesScreen
import `in`.rahulja.medicinekit.ui.screens.MedicineScreen
import `in`.rahulja.medicinekit.ui.screens.MedicinesScreen
import `in`.rahulja.medicinekit.ui.screens.ScannerScreen
import `in`.rahulja.medicinekit.ui.screens.SettingsScreen

@Composable
fun appNavGraph(navigator: Navigator, state: NavigationState): (NavKey) -> NavEntry<NavKey> = entryProvider {
    // Bottom menu items //
    entry<Screen.Medicines>(
        metadata = NavDisplay.predictivePopTransitionSpec {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween()),
                initialContentExit = fadeOut(animationSpec = tween()),
            )
        }
    ) {
        MedicinesScreen(onNavigate = navigator::navigate)
    }
    entry<Screen.Intakes>(
        metadata = NavDisplay.predictivePopTransitionSpec {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween()),
                initialContentExit = fadeOut(animationSpec = tween()),
            )
        }
    ) {
        IntakesScreen { navigator.navigate(Screen.Intake(intakeId = it)) }
    }
    entry<Screen.Settings>(
        metadata = NavDisplay.predictivePopTransitionSpec {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween()),
                initialContentExit = fadeOut(animationSpec = tween()),
            )
        }
    ) {
        SettingsScreen { navigator.navigate(Screen.Auth()) }
    }

    // Screens //
    entry<Screen.Auth>(
        metadata = NavDisplay.predictivePopTransitionSpec {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween()),
                initialContentExit = fadeOut(animationSpec = tween()),
            )
        }
    ) {
        AuthScreen(
            model = koinViewModel { parametersOf(it.code) },
            onBack = navigator::goBack
        )
    }
    entry<Screen.Scanner> {
        ScannerScreen(
            onBack = navigator::goBack,
            onNavigate = navigator::navigate
        )
    }
    entry<Screen.Medicine> { (id, cis, duplicate, openCamera) ->
        MedicineScreen(
            model = koinViewModel { parametersOf(id, cis, duplicate, openCamera) },
            onBack = { navigator.navigateAndClearStack(Screen.Medicines) },
            onGoToIntake = { navigator.navigate(Screen.Intake(medicineId = it)) }
        )
    }
    entry<Screen.Intake> { (intakeId, medicineId) ->
        IntakeScreen(
            model = koinViewModel { parametersOf(intakeId, medicineId) },
            onBack = navigator::goBack
        )
    }
    entry<Screen.IntakeFullScreen> { (takenId, medicineId, amount) ->
        val onBack = when {
            state.previousRoute is Screen.IntakeFullScreen -> navigator::goBack
            state.currentStack.size <= 2 && state.currentStack.firstOrNull() is Screen.Intakes -> null
            else -> navigator::goBack
        }

        IntakeFullScreen(medicineId, takenId, amount, onBack)
    }
}
