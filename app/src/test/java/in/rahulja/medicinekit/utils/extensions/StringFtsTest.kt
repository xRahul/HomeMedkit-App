package `in`.rahulja.medicinekit.utils.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringFtsTest {

    @Test
    fun `test simple string escaping`() {
        assertEquals("\"Aspirin\"", "Aspirin".escapeFts())
    }

    @Test
    fun `test string with double quotes escaping`() {
        assertEquals("\"Aspirin \"\"81mg\"\"\"", "Aspirin \"81mg\"".escapeFts())
    }

    @Test
    fun `test string with special fts characters escaping`() {
        // Special characters should be literal within double quotes
        assertEquals("\"Aspirin-Extra*\"", "Aspirin-Extra*".escapeFts())
        assertEquals("\"Aspirin OR Paracetamol\"", "Aspirin OR Paracetamol".escapeFts())
        assertEquals("\"NOT Aspirin\"", "NOT Aspirin".escapeFts())
    }

    @Test
    fun `test empty string escaping`() {
        assertEquals("\"\"", "".escapeFts())
    }

    @Test
    fun `test string with only quotes escaping`() {
        assertEquals("\"\"\"\"", "\"".escapeFts())
        assertEquals("\"\"\"\"\"\"", "\"\"".escapeFts())
    }
}
