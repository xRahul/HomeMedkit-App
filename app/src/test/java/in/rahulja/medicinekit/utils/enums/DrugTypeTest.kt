package `in`.rahulja.medicinekit.utils.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DrugTypeTest {

    @Test
    fun `setIcon returns correct value for tablets`() {
        val result = DrugType.setIcon("ТАБЛЕТКИ")
        assertEquals(DrugType.TABLETS.value, result)
    }

    @Test
    fun `setIcon returns correct value for tablets partial match`() {
        val result = DrugType.setIcon("ТАБЛЕТКА")
        assertEquals(DrugType.TABLETS.value, result)
    }

    @Test
    fun `setIcon returns empty for unknown type`() {
        val result = DrugType.setIcon("UNKNOWN")
        assertEquals("", result)
    }

    @Test
    fun `getDoseType returns correct type for capsules`() {
        val result = DrugType.getDoseType("КАПСУЛЫ")
        assertEquals(DoseType.PIECES, result)
    }

    @Test
    fun `getDoseType returns UNKNOWN for unknown type`() {
        val result = DrugType.getDoseType("UNKNOWN")
        assertEquals(DoseType.UNKNOWN, result)
    }

    @Test
    fun `setIcon handles case insensitivity`() {
        val result = DrugType.setIcon("таблетки")
        assertEquals(DrugType.TABLETS.value, result)
    }
}
