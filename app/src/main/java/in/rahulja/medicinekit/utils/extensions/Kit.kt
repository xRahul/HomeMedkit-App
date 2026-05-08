package `in`.rahulja.medicinekit.utils.extensions

import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.data.model.KitModel
import `in`.rahulja.medicinekit.utils.ResourceText

fun Kit.toModel() = KitModel(
    id = kitId,
    position = position,
    title = if (kitId > 0) ResourceText.StaticString(title)
    else ResourceText.StringResource(R.string.text_no_group)
)