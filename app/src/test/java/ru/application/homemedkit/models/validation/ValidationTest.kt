package ru.application.homemedkit.models.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.IntakeAmountTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState

@OptIn(ExperimentalMaterial3Api::class)
class ValidationTest {

    @Test
    fun `checkAmount returns success for valid amounts`() {
        val list = listOf(
            IntakeAmountTime("1.5", "12:00", TimePickerState(12, 0, true)),
            IntakeAmountTime("2", "18:00", TimePickerState(18, 0, true))
        )
        val result = Validation.checkAmount(list)
        assertTrue(result.successful)
    }

    @Test
    fun `checkAmount returns failure for empty amount`() {
        val list = listOf(IntakeAmountTime("", "12:00", TimePickerState(12, 0, true)))
        val result = Validation.checkAmount(list)
        assertFalse(result.successful)
        assertEquals(R.string.text_fill_field, result.errorMessage)
    }

    @Test
    fun `checkAmount returns failure for invalid number`() {
        val list = listOf(IntakeAmountTime("abc", "12:00", TimePickerState(12, 0, true)))
        val result = Validation.checkAmount(list)
        assertFalse(result.successful)
        assertEquals(R.string.text_fill_field, result.errorMessage)
    }

    @Test
    fun `checkTime returns success for valid times`() {
        val list = listOf(IntakeAmountTime("1.0", "12:00", TimePickerState(12, 0, true)))
        val result = Validation.checkTime(list)
        assertTrue(result.successful)
    }

    @Test
    fun `checkTime returns failure for empty time`() {
        val list = listOf(IntakeAmountTime("1.0", "", TimePickerState(12, 0, true)))
        val result = Validation.checkTime(list)
        assertFalse(result.successful)
        assertEquals(R.string.text_fill_field, result.errorMessage)
    }

    @Test
    fun `textNotEmpty returns success for non-empty text`() {
        val result = Validation.textNotEmpty("something")
        assertTrue(result.successful)
    }

    @Test
    fun `textNotEmpty returns failure for empty text`() {
        val result = Validation.textNotEmpty("")
        assertFalse(result.successful)
        assertEquals(R.string.text_fill_field, result.errorMessage)
    }
}
