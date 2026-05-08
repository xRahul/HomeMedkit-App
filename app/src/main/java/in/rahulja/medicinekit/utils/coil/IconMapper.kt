package `in`.rahulja.medicinekit.utils.coil

import android.content.Context
import coil3.map.Mapper
import coil3.request.Options
import `in`.rahulja.medicinekit.utils.enums.DrugType
import java.io.File

class IconMapper(context: Context) : Mapper<String, Any> {
    private val filesDir = context.filesDir

    override fun map(data: String, options: Options) =
        DrugType.iconMap[data] ?: File(filesDir, data)
}