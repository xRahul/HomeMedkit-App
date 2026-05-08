package `in`.rahulja.medicinekit.data.model

interface IntakeListScheme<T> {
    val epochDay: Long
    val date: String
    val intakes: List<T>
}