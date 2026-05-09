package `in`.rahulja.medicinekit.models.events

import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.models.states.MedicineDialogState
import `in`.rahulja.medicinekit.utils.AiMedicineResult
import `in`.rahulja.medicinekit.utils.enums.AiMode
import `in`.rahulja.medicinekit.utils.enums.DoseType

sealed interface MedicineEvent {
    data class SetProductName(val productName: String) : MedicineEvent
    data class SetNameAlias(val alias: String) : MedicineEvent
    data class SetExpDate(val month: Int, val year: Int) : MedicineEvent
    data class SetPackageDate(val timestamp: Long) : MedicineEvent
    data object ClearPackageDate : MedicineEvent
    data class SetFormName(val formName: String) : MedicineEvent
    data class SetDoseName(val doseName: String) : MedicineEvent
    data class SetDoseType(val type: DoseType) : MedicineEvent
    data class SetAmount(val amount: String) : MedicineEvent
    data class SetPhKinetics(val phKinetics: String) : MedicineEvent
    data class SetSalts(val salts: String) : MedicineEvent
    data class SetComment(val comment: String) : MedicineEvent
    data class SetExtractedImagesText(val text: String) : MedicineEvent
    data class PickKit(val kit: Kit) : MedicineEvent
    data object ClearKit : MedicineEvent
    data object MakeDuplicate : MedicineEvent

    data class SetImage(val fileName: String?) : MedicineEvent
    data class RemoveImage(val image: String) : MedicineEvent
    data class SetIcon(val icon: String) : MedicineEvent

    data class ProcessImageWithAi(
        val uri: android.net.Uri,
        val useAi: Boolean,
        val aiMode: AiMode,
        val apiKey: String
    ) : MedicineEvent

    data object ExtractTextFromImages : MedicineEvent
    data class AnalyzeTextWithGemini(val apiKey: String) : MedicineEvent

    data class OnImageReodering(val fromIndex: Int, val toIndex: Int) : MedicineEvent

    data object EditImagesOrder : MedicineEvent

    data class ToggleDialog(val dialog: MedicineDialogState) : MedicineEvent

    data object ApplyAiResult : MedicineEvent
}
