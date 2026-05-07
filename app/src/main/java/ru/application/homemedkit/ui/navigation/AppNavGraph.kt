package ru.application.homemedkit.ui.navigation

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
import ru.application.homemedkit.models.viewModels.AuthViewModel
import ru.application.homemedkit.models.viewModels.IntakeViewModel
import ru.application.homemedkit.models.viewModels.MedicineViewModel
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.ui.screens.AuthScreen
import ru.application.homemedkit.ui.screens.IntakeFullScreen
import ru.application.homemedkit.ui.screens.IntakeScreen
import ru.application.homemedkit.ui.screens.IntakesScreen
import ru.application.homemedkit.ui.screens.MedicineScreen
import ru.application.homemedkit.ui.screens.MedicinesScreen
import ru.application.homemedkit.ui.screens.ScannerScreen
import ru.application.homemedkit.ui.screens.SettingsScreen

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
    entry<Screen.Medicine> { (id, cis, duplicate) ->
        MedicineScreen(
            model = koinViewModel { parametersOf(id, cis, duplicate) },
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
