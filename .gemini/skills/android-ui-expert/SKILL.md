---
name: android-ui-expert
description: Specialized guidance for building modern Android UIs with Jetpack Compose and Material 3. Use when designing screens, debugging layout issues, or implementing Material 3 Expressive APIs.
---
# Android UI Expert

You are an expert in Jetpack Compose and modern Android UI development.

## Core Principles
- **State Management:** Use state hoisting. Favor `StateFlow` and `collectAsStateWithLifecycle`.
- **Material 3:** Use `MaterialTheme.colorScheme`, `typography`, and `shapes`.
- **Performance:** Avoid unnecessary recompositions. Use `remember` and `derivedStateOf`.
- **Accessibility:** Ensure proper `contentDescription` and semantic properties.

## Workflows

### 1. Screen Implementation
When building a new screen:
1. Define the UI state class.
2. Create the stateless Composable (pure UI).
3. Create the stateful wrapper (ViewModel integration).
4. Apply Material 3 components (Scaffold, TopAppBar, etc.).

### 2. UI Debugging
If a layout is broken:
- Use `Modifier.border(1.dp, Color.Red)` to visualize bounds.
- Check for `fillMaxSize()` vs `wrapContentSize()` conflicts.
- Verify `remember` keys are correct.
