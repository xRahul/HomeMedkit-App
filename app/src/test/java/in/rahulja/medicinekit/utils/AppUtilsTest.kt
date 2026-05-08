package `in`.rahulja.medicinekit.utils

import androidx.compose.foundation.text.input.TextFieldBuffer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class AppUtilsTest {

    @Test
    fun `DecimalAmountInputTransformation reverts changes for non-numeric input`() {
        val buffer = mockk<TextFieldBuffer>(relaxed = true)
        every { buffer.asCharSequence() } returns "abc"
        
        with(DecimalAmountInputTransformation) {
            buffer.transformInput()
        }
        
        verify { buffer.revertAllChanges() }
    }

    @Test
    fun `DecimalAmountInputTransformation replaces comma with dot`() {
        val buffer = mockk<TextFieldBuffer>(relaxed = true)
        every { buffer.asCharSequence() } returns "1,5"
        every { buffer.length } returns 3
        
        with(DecimalAmountInputTransformation) {
            buffer.transformInput()
        }
        
        verify { buffer.replace(0, 3, "1.5") }
    }

    @Test
    fun `DaysInputTransformation reverts changes for non-integer input`() {
        val buffer = mockk<TextFieldBuffer>(relaxed = true)
        every { buffer.asCharSequence() } returns "1.5"
        
        with(DaysInputTransformation) {
            buffer.transformInput()
        }
        
        verify { buffer.revertAllChanges() }
    }
}
