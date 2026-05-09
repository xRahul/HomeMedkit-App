package `in`.rahulja.medicinekit.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import java.io.IOException
import kotlin.math.max

@Serializable
data class AiMedicineResult(
    val name: String = "",
    val salts: String = "",
    val dose: String = "",
    val form: String = "",
    val composition: String = "",
    val indications: String = "",
    val recommendations: String = "",
    val storage: String = "",
    val comment: String = ""
)

object AiMedicineParser {

    suspend fun parseWithMLKit(context: Context, imageUri: Uri): String {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            BLANK
        }
    }

    suspend fun parseWithGemini(context: Context, imageUri: Uri?, apiKey: String, useImage: Boolean, extractedText: String = ""): AiMedicineResult? {
        if (apiKey.isBlank()) return null
        return try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val prompt = """
                Extract medicine information and format it as a strictly valid JSON object with these exact keys:
                "name", "salts" (active ingredients), "dose", "form" (release form like tablet, syrup, etc.),
                "composition", "indications", "recommendations", "storage".
                
                CRITICAL INSTRUCTION:
                1. First, identify the medicine from the image/text.
                2. Second, SEARCH YOUR KNOWLEDGE BASE for the full details of this medicine (especially if it is a common medicine from India).
                3. Third, even if some information is not clearly visible on the packaging (like full indications or storage conditions), provide the standard information from your knowledge base for this specific medicine.
                4. Fill all fields as accurately as possible. Use empty string "" only if the medicine itself is unknown.
                
                Respond ONLY with the raw JSON object, no markdown formatting blocks, no extra text.
            """.trimIndent()

            val response = if (useImage && imageUri != null) {
                val bitmap = getBitmapFromUri(context, imageUri) ?: return null
                generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
            } else {
                generativeModel.generateContent(
                    content {
                        text("Here is the OCR text from a medicine box/label: $extractedText\n\n$prompt")
                    }
                )
            }

            val jsonString = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: return null
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<AiMedicineResult>(jsonString)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            val maxDimension = 1024
            val width = bitmap.width
            val height = bitmap.height

            if (max(width, height) > maxDimension) {
                val scale = maxDimension.toFloat() / max(width, height)
                val matrix = Matrix()
                matrix.postScale(scale, scale)
                Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}
