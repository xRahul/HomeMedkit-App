package ru.application.homemedkit.models.validation

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.IntakeAmountTime

class ValidationTest {

    @Test
    fun `checkTime returns successful when all times are not empty`() {
        val list = listOf(
            IntakeAmountTime(time = "10:00", picker = mockk()),
            IntakeAmountTime(time = "14:00", picker = mockk())
        )

        val result = Validation.checkTime(list)

        assertTrue(result.successful)
        assertNull(result.errorMessage)
    }

    @Test
    fun `checkTime returns failure when at least one time is empty`() {
        val list = listOf(
            IntakeAmountTime(time = "10:00", picker = mockk()),
            IntakeAmountTime(time = "", picker = mockk())
        )

        val result = Validation.checkTime(list)

        assertFalse(result.successful)
        assertEquals(R.string.text_fill_field, result.errorMessage)
    }

    @Test
    fun `checkTime returns successful when list is empty`() {
        val list = emptyList<IntakeAmountTime>()

        val result = Validation.checkTime(list)

        assertTrue(result.successful)
        assertNull(result.errorMessage)
    }

    @Test
    fun `checkAmount returns successful when all amounts are not empty`() {
        val list = listOf(
            IntakeAmountTime(amount = "1", picker = mockk()),
            IntakeAmountTime(amount = "0.5", picker = mockk())
        )

        val result = Validation.checkAmount(list)

        assertTrue(result.successful)
        assertNull(result.errorMessage)
    }

    @Test
    fun `checkAmount returns failure when at least one amount is empty`() {
        val list = listOf(
            IntakeAmountTime(amount = "1", picker = mockk()),
            IntakeAmountTime(amount = "", picker = mockk())
        )

        val result = Validation.checkAmount(list)

        assertFalse(result.successful)
        assertEquals(R.string.text_fill_field, result.errorMessage)
    }

    @Test
    fun `textNotEmpty returns successful when text is not empty`() {
        val text = "some text"

        val result = Validation.textNotEmpty(text)

        assertTrue(result.successful)
        assertNull(result.errorMessage)
    }

    @Test
    fun `textNotEmpty returns failure when text is empty`() {
        val text = ""

        val result = Validation.textNotEmpty(text)

        assertFalse(result.successful)
        assertEquals(R.string.text_fill_field, result.errorMessage)
    }
}
