package `in`.rahulja.medicinekit.domain.usecases

import android.content.Context
import android.net.Uri
import `in`.rahulja.medicinekit.utils.AiMedicineParser
import `in`.rahulja.medicinekit.utils.AiMedicineResult
import `in`.rahulja.medicinekit.utils.enums.AiMode
import java.io.File

class MedicineParserUseCase(
    private val context: Context,
) {
    suspend fun extractTextFromImages(images: List<String>): String {
        val texts = mutableListOf<String>()
        for (imageName in images) {
            val uri = Uri.fromFile(File(context.filesDir, imageName))
            val text = AiMedicineParser.parseWithMLKit(context, uri)
            if (text.isNotBlank()) {
                texts.add(text.replace("\n", " ").trim())
            }
        }
        return texts.joinToString("\n\n")
    }

    suspend fun analyzeTextWithGemini(apiKey: String, extractedText: String): AiMedicineResult? {
        return AiMedicineParser.parseWithGemini(
            context = context,
            imageUri = null,
            apiKey = apiKey,
            useImage = false,
            extractedText = extractedText,
        )
    }

    suspend fun processImageWithAi(uri: Uri, apiKey: String, aiMode: AiMode): AiMedicineResult? {
        val extractedText = AiMedicineParser.parseWithMLKit(context, uri)

        return if (aiMode == AiMode.ML_KIT) {
            val cleanedText = extractedText.replace("\n", " ").trim()
            AiMedicineResult(
                name = cleanedText.take(50),
                comment = cleanedText,
            )
        } else {
            AiMedicineParser.parseWithGemini(
                context = context,
                imageUri = uri,
                apiKey = apiKey,
                useImage = true,
                extractedText = extractedText,
            )
        }
    }
}
