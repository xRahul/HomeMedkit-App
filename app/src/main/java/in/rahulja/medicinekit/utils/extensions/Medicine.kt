package `in`.rahulja.medicinekit.utils.extensions

import `in`.rahulja.medicinekit.data.dto.Image
import `in`.rahulja.medicinekit.data.dto.Medicine
import `in`.rahulja.medicinekit.data.model.MedicineFull
import `in`.rahulja.medicinekit.data.model.MedicineIntake
import `in`.rahulja.medicinekit.data.model.MedicineList
import `in`.rahulja.medicinekit.data.model.MedicineMain
import `in`.rahulja.medicinekit.models.states.MedicineState
import `in`.rahulja.medicinekit.models.states.TechnicalState
import `in`.rahulja.medicinekit.network.models.MainModel
import `in`.rahulja.medicinekit.network.models.bio.BioData
import `in`.rahulja.medicinekit.network.models.medicine.DrugsData
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.ResourceText
import `in`.rahulja.medicinekit.utils.enums.DrugType

fun MedicineFull.toState() = MedicineState(
    adding = false,
    editing = false,
    default = true,
    isLoading = false,
    isOpened = packageOpenedDate > 0L,
    id = id,
    kits = kits.toSet(),
    code = cis,
    productName = productName,
    nameAlias = nameAlias.ifEmpty { productName },
    expDate = expDate,
    expDateString = Formatter.toExpDate(expDate),
    dateOpened = packageOpenedDate,
    dateOpenedString = Formatter.toExpDate(packageOpenedDate),
    prodFormNormName = prodFormNormName,
    structure = structure,
    prodDNormName = prodDNormName,
    prodAmount = prodAmount.toString(),
    doseType = doseType,
    phKinetics = phKinetics,
    recommendations = recommendations,
    storageConditions = storageConditions,
    comment = comment,
    extractedImagesText = extractedImagesText,
    images = images.sortedBy(Image::position).map(Image::image),
    technical = TechnicalState(
        scanned = scanned,
        verified = verified
    )
)

fun MedicineFull.toMedicineIntake() = MedicineIntake(
    productName = productName,
    nameAlias = nameAlias,
    prodFormNormName = prodFormNormName,
    expDate = expDate,
    prodAmount = prodAmount,
    doseType = doseType
)

fun MedicineMain.toMedicineList(currentMillis: Long) = MedicineList(
    id = id,
    title = nameAlias.ifEmpty(::productName),
    prodAmountDoseType = ResourceText.MultiString(
        value = listOf(
            ResourceText.StaticString(Formatter.decimalFormat(prodAmount)),
            ResourceText.StringResource(doseType.title)
        )
    ),
    expDateS = Formatter.cardFormat(expDate),
    formName = Formatter.formFormat(prodFormNormName),
    image = image.orEmpty(),
    inStock = prodAmount >= 0.1,
    isExpired = expDate < currentMillis,
    kitIds = kitIds
)

fun MedicineState.toMedicine() = Medicine(
    id = id,
    cis = code,
    productName = productName,
    salts = salts,
    nameAlias = nameAlias,
    expDate = expDate,
    packageOpenedDate = dateOpened,
    prodFormNormName = prodFormNormName,
    structure = structure,
    prodDNormName = prodDNormName,
    prodAmount = prodAmount.toDoubleOrNull() ?: 0.0,
    doseType = doseType,
    phKinetics = phKinetics,
    recommendations = recommendations,
    storageConditions = storageConditions,
    comment = comment,
    extractedImagesText = extractedImagesText,
    scanned = code.isNotBlank(),
    verified = technical.verified
)

fun MainModel.asMedicine() = drugsData?.toMedicine() ?: bioData?.toMedicine() ?: toMedicine()

private fun DrugsData.toMedicine() = Medicine(
    productName = prodDescLabel,
    expDate = expireDate,
    prodFormNormName = foiv.prodFormNormName,
    prodDNormName = foiv.prodDNormName.orEmpty(),
    doseType = DrugType.getDoseType(foiv.prodFormNormName),
    phKinetics = vidalData?.phKinetics.orEmpty().asHtml(),
    scanned = true,
    verified = true,
    prodAmount = foiv.prodPack1Size?.let { (it.toDoubleOrNull() ?: 0.0) * (foiv.prodPack12?.toDoubleOrNull() ?: 1.0) } ?: 0.0
)

private fun BioData.toMedicine() = Medicine(
    productName = productName,
    expDate = expireDate ?: 0L,
    prodDNormName = productProperty?.unitVolumeWeight.orEmpty(),
    prodAmount = productProperty?.quantityInPack ?: 0.0,
    phKinetics = productProperty?.applicationArea.orEmpty().asHtml(),
    recommendations = productProperty?.recommendForUse.orEmpty().asHtml(),
    storageConditions = productProperty?.storageConditions.orEmpty().asHtml(),
    structure = productProperty?.structure.orEmpty().asHtml(),
    prodFormNormName = productProperty?.releaseForm.orEmpty().substringBefore(" ").uppercase(),
    doseType = DrugType.getDoseType(productProperty?.releaseForm.orEmpty()),
    scanned = true,
    verified = true
)

private fun MainModel.toMedicine() = Medicine(
    productName = productName,
    prodAmount = 0.0,
    scanned = true,
    verified = true
)