package ru.application.homemedkit.utils

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import androidx.compose.ui.text.intl.Locale
import java.util.Locale as JavaLocale

class FormatterTest {

    @BeforeEach
    fun setup() {
        // Mock android.icu.text.NumberFormat
        mockkStatic(android.icu.text.NumberFormat::class)
        val mockDecimalFormat = mockk<android.icu.text.DecimalFormat>()
        every { android.icu.text.NumberFormat.getInstance(any<JavaLocale>()) } returns mockDecimalFormat
        every { mockDecimalFormat.setMaximumFractionDigits(any()) } returns Unit
        // Mock decimalFormat behavior
        every { mockDecimalFormat.format(any<Double>()) } answers { it.invocation.args[0].toString() }
        every { mockDecimalFormat.parse(any()) } answers { it.invocation.args[0].toString().replace(",", ".").toDouble() }

        mockkObject(Locale.Companion)
        val mockPlatformLocale = JavaLocale.US
        val mockLocale = mockk<Locale>()
        every { mockLocale.platformLocale } returns mockPlatformLocale
        every { Locale.current } returns mockLocale
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `formFormat returns first part`() {
        assertEquals("ТАБЛЕТКИ", Formatter.formFormat("ТАБЛЕТКИ (10 шт)"))
    }
}
