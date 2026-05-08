package `in`.rahulja.medicinekit.models.viewModels

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import `in`.rahulja.medicinekit.data.dao.KitDAO
import `in`.rahulja.medicinekit.data.dao.MedicineDAO
import `in`.rahulja.medicinekit.data.dto.Image
import `in`.rahulja.medicinekit.data.dto.MedicineKit
import `in`.rahulja.medicinekit.models.events.MedicineAction
import `in`.rahulja.medicinekit.models.events.MedicineEvent
import `in`.rahulja.medicinekit.models.events.Response
import `in`.rahulja.medicinekit.models.states.MedicineDialogState
import `in`.rahulja.medicinekit.models.states.MedicineState
import `in`.rahulja.medicinekit.models.validation.Validation
import `in`.rahulja.medicinekit.network.Network
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.enums.DoseType
import `in`.rahulja.medicinekit.utils.enums.DrugType
import `in`.rahulja.medicinekit.utils.enums.ImageEditing
import `in`.rahulja.medicinekit.utils.extensions.asMedicine
import `in`.rahulja.medicinekit.utils.extensions.concat
import `in`.rahulja.medicinekit.utils.extensions.toMedicine
import `in`.rahulja.medicinekit.utils.extensions.toState
import `in`.rahulja.medicinekit.utils.extensions.toggle
import `in`.rahulja.medicinekit.utils.getMedicineImages
import java.io.File
import `in`.rahulja.medicinekit.data.dto.Kit

class MedicineViewModel(
    private val id: Long,
    private val cis: String,
    private val duplicate: Boolean,
    private val openCamera: Boolean,
    private val dao: MedicineDAO,
    private val daoK: KitDAO
) : BaseViewModel<MedicineState, MedicineEvent>() {

    private val _action = Channel<MedicineAction>()
    val action = _action.receiveAsFlow()

    val kits = daoK.getFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList<Kit>())

    init {
        if (openCamera) {
            updateState { it.copy(dialogState = MedicineDialogState.TakePhoto) }
        }
    }

    override fun initState() = MedicineState()

    override fun loadData() {
        viewModelScope.launch {
            val medicine = dao.getById(id)

            if (medicine != null) {
                val newState = withContext(Dispatchers.Default) { medicine.toState() }

                updateState { newState }
            } else {
                updateState {
                    it.copy(
                        adding = true,
                        isLoading = false,
                        code = cis,
                        images = if (openCamera) emptyList() else listOf(DrugType.entries.random().value)
                    )
                }
            }

            if (duplicate) {
                _action.send(MedicineAction.ShowSnackbar.OnReceiveDuplicate)
            }
        }
    }

    fun add() {
        viewModelScope.launch {
            val checkProductName = Validation.textNotEmpty(currentState.productName)

            if (checkProductName.successful) {
                val id = dao.insert(currentState.toMedicine())

                coroutineScope {
                    val jobOne = launch {
                        val kits = currentState.kits.map { MedicineKit(id, it.kitId) }

                        daoK.pinKit(kits)
                    }

                    val jobTwo = launch {
                        val images = currentState.images.mapIndexed { index, image ->
                            Image(
                                medicineId = id,
                                position = index,
                                image = image
                            )
                        }

                        dao.updateImages(images)
                    }

                    joinAll(jobOne, jobTwo)
                }

                updateState {
                    it.copy(
                        id = id,
                        adding = false,
                        default = true,
                        productNameError = null
                    )
                }
            } else {
                updateState { it.copy(productNameError = checkProductName.errorMessage) }
            }
        }
    }

    fun fetch(dir: File) {
        viewModelScope.launch {
            updateState { it.copy(dialogState = MedicineDialogState.Loading) }

            try {
                when (val response = Network.getMedicine(currentState.code)) {
                    is Response.Success -> {
                        val medicine = response.model.asMedicine().copy(
                            id = currentState.id,
                            cis = currentState.code,
                            comment = currentState.comment.ifEmpty { BLANK }
                        )

                        val images = async {
                            getMedicineImages(
                                medicineId = currentState.id,
                                form = medicine.prodFormNormName,
                                directory = dir,
                                urls = response.model.imageUrls
                            )
                        }

                        if (currentState.id != 0L) {
                            coroutineScope {
                                val jobOne = launch { dao.update(medicine) }
                                val jobTow = launch { dao.updateImages(images.await()) }

                                joinAll(jobOne, jobTow)
                            }

                            dao.getById(id)?.let { medicineFull ->
                                updateState { medicineFull.toState() }
                            }
                        } else {
                            val fetchedImages = images.await().map { it.image }
                            updateState {
                                it.copy(
                                    productName = medicine.productName.ifEmpty { it.productName },
                                    expDate = if (it.expDate == -1L) medicine.expDate else it.expDate,
                                    expDateString = if (it.expDate == -1L) Formatter.toExpDate(medicine.expDate) else it.expDateString,
                                    prodFormNormName = medicine.prodFormNormName.ifEmpty { it.prodFormNormName },
                                    prodDNormName = medicine.prodDNormName.ifEmpty { it.prodDNormName },
                                    doseType = if (it.doseType == DoseType.UNKNOWN) medicine.doseType else it.doseType,
                                    phKinetics = medicine.phKinetics.ifEmpty { it.phKinetics },
                                    prodAmount = if (it.prodAmount == BLANK) medicine.prodAmount.toString() else it.prodAmount,
                                    images = (it.images + fetchedImages).distinct()
                                )
                            }
                        }
                    }

                    is Response.Error -> {
                        _action.send(MedicineAction.ShowSnackbar.OnShowError(response.message))
                    }
                }
            } catch (_: Exception) {
                _action.send(MedicineAction.ShowSnackbar.OnShowError())
            } finally {
                updateState { it.copy(dialogState = null) }
            }
        }
    }

    fun fetchImages(dir: File) {
        viewModelScope.launch {
            updateState { it.copy(dialogState = MedicineDialogState.Loading) }

            try {
                when (val response = Network.getMedicine(currentState.code)) {
                    is Response.Success -> {
                        val images = getMedicineImages(
                            medicineId = currentState.id,
                            form = currentState.prodFormNormName,
                            directory = dir,
                            urls = response.model.imageUrls
                        )

                        dao.updateImages(images)

                        dao.getById(id)?.let { medicine ->
                            updateState { medicine.toState() }
                        }
                    }

                    is Response.Error -> {
                        _action.send(MedicineAction.ShowSnackbar.OnShowError(response.message))
                    }
                }
            } catch (_: Exception) {
                _action.send(MedicineAction.ShowSnackbar.OnShowError())
            } finally {
                updateState { it.copy(dialogState = null) }
            }
        }
    }

    fun update() {
        viewModelScope.launch {
            val checkProductName = Validation.textNotEmpty(currentState.productName)

            if (checkProductName.successful) {
                val kits = currentState.kits.map { MedicineKit(currentState.id, it.kitId) }
                val images = currentState.images.mapIndexed { index, image ->
                    Image(
                        medicineId = currentState.id,
                        position = index,
                        image = image
                    )
                }

                daoK.deleteAll(currentState.id)
                daoK.pinKit(kits)
                dao.updateImages(images)

                dao.update(currentState.toMedicine())

                dao.getById(currentState.id)?.let { medicine ->
                    updateState { medicine.toState() }
                }
            } else {
                updateState { it.copy(productNameError = checkProductName.errorMessage) }
            }
        }
    }

    fun delete(dir: File) {
        viewModelScope.launch {
            coroutineScope {
                launch { dao.delete(currentState.toMedicine()) }
                launch(Dispatchers.IO) {
                    currentState.images.forEach {
                        if (dao.getImageCount(it) == 0) {
                            File(dir, it).delete()
                        }
                    }
                }
            }

            updateState { it.copy(dialogState = null) }

            _action.send(MedicineAction.OnDelete)
        }
    }

     override fun onEvent(event: MedicineEvent) {
        when (event) {
            is MedicineEvent.SetProductName -> updateState { it.copy(productName = event.productName) }
            is MedicineEvent.SetNameAlias -> updateState { it.copy(nameAlias = event.alias) }
            is MedicineEvent.SetExpDate -> {
                val expDate = Formatter.toTimestamp(event.month, event.year)

                updateState {
                    it.copy(
                        expDate = expDate,
                        expDateString = Formatter.toExpDate(expDate),
                        dialogState = null
                    )
                }
            }
            is MedicineEvent.SetPackageDate -> updateState {
                it.copy(
                    dateOpened = event.timestamp,
                    dateOpenedString = Formatter.toExpDate(event.timestamp),
                    dialogState = null,
                    isOpened = event.timestamp > 0L
                )
            }
            is MedicineEvent.SetFormName -> updateState { it.copy(prodFormNormName = event.formName) }
            is MedicineEvent.SetDoseName -> updateState { it.copy(prodDNormName = event.doseName) }
            is MedicineEvent.SetDoseType -> updateState { it.copy(doseType = event.type) }
            is MedicineEvent.SetAmount -> updateState { it.copy(prodAmount = event.amount) }
            is MedicineEvent.SetPhKinetics -> updateState { it.copy(phKinetics = event.phKinetics) }
            is MedicineEvent.SetSalts -> updateState { it.copy(salts = event.salts) }
            is MedicineEvent.SetComment -> updateState { it.copy(comment = event.comment) }
            is MedicineEvent.PickKit -> updateState { it.copy(kits = it.kits.toggle(event.kit)) }

            is MedicineEvent.ProcessImageWithAi -> {
                updateState { it.copy(isLoading = false, loadingMessage = "Extracting text from image...", dialogState = MedicineDialogState.Loading) }
                viewModelScope.launch {
                    try {
                        val extractedText = `in`.rahulja.medicinekit.utils.AiMedicineParser.parseWithMLKit(event.context, event.uri)
                        if (event.useAi) {
                            if (event.aiMode == `in`.rahulja.medicinekit.utils.enums.AiMode.ML_KIT) {
                                val cleanedText = extractedText.replace("\n", " ").trim()
                                updateState { it.copy(loadingMessage = "Autofilling form...") }
                                updateState {
                                    it.copy(
                                        productName = if (it.productName.isBlank()) cleanedText.take(50) else it.productName,
                                        comment = if (it.comment.isBlank()) cleanedText.take(100) else it.comment,
                                        isLoading = false,
                                        loadingMessage = null
                                    )
                                }
                            } else if (event.aiMode == `in`.rahulja.medicinekit.utils.enums.AiMode.GEMINI) {
                                updateState { it.copy(loadingMessage = "Structuring data with AI...") }
                                val result = `in`.rahulja.medicinekit.utils.AiMedicineParser.parseWithGemini(
                                    context = event.context,
                                    imageUri = event.uri,
                                    apiKey = event.apiKey,
                                    useImage = true,
                                    extractedText = extractedText
                                )
                                if (result != null) {
                                    updateState { it.copy(loadingMessage = "Autofilling form...") }
                                    updateState {
                                        it.copy(
                                            productName = result.name.ifBlank { it.productName },
                                            salts = result.salts.ifBlank { it.salts },
                                            prodDNormName = result.dose.ifBlank { it.prodDNormName },
                                            prodFormNormName = result.form.ifBlank { it.prodFormNormName },
                                            structure = result.composition.ifBlank { it.structure },
                                            phKinetics = result.indications.ifBlank { it.phKinetics },
                                            recommendations = result.recommendations.ifBlank { it.recommendations },
                                            storageConditions = result.storage.ifBlank { it.storageConditions },
                                            isLoading = false,
                                            loadingMessage = null
                                        )
                                    }
                                } else {
                                    updateState { it.copy(isLoading = false, loadingMessage = null) }
                                    _action.send(MedicineAction.ShowSnackbar.OnShowError())
                                }
                            }
                        } else {
                            updateState { it.copy(isLoading = false, loadingMessage = null) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateState { it.copy(isLoading = false, loadingMessage = null) }
                        _action.send(MedicineAction.ShowSnackbar.OnShowError())
                    }
                }
            }

            MedicineEvent.ClearKit -> updateState { it.copy(kits = emptySet()) }

            is MedicineEvent.SetIcon -> updateState {
                it.copy(
                    dialogState = MedicineDialogState.PictureGrid,
                    images = it.images.concat(event.icon),
                )
            }

            is MedicineEvent.SetImage -> if (event.fileName != null) {
                updateState {
                    it.copy(
                        dialogState = MedicineDialogState.PictureGrid,
                        images = it.images.concat(event.fileName)
                    )
                }
            } else {
                updateState {
                    it.copy(dialogState = MedicineDialogState.PictureGrid)
                }
            }

            is MedicineEvent.OnImageReodering -> {
                val imagesNameMutable = currentState.images.toMutableList()
                val removedName = imagesNameMutable.removeAt(event.fromIndex)

                imagesNameMutable.add(event.toIndex, removedName)

                updateState {
                    it.copy(images = imagesNameMutable)
                }
            }

            is MedicineEvent.RemoveImage -> {
                val imageNames = currentState.images.toMutableList().apply {
                    remove(event.image)
                }

                updateState {
                    it.copy(images = imageNames)
                }
            }

            MedicineEvent.EditImagesOrder -> updateState {
                it.copy(
                    images = it.images.distinct(),
                    imageEditing = ImageEditing.entries.getOrElse(it.imageEditing.ordinal + 1) { ImageEditing.ADDING }
                )
            }

            MedicineEvent.ClearPackageDate -> updateState {
                it.copy(
                    dateOpened = -1L,
                    dateOpenedString = BLANK,
                    dialogState = null
                )
            }

            is MedicineEvent.ToggleDialog -> updateState {
                if (it.dialogState == event.dialog) {
                    it.copy(
                        dialogState = when (event.dialog) {
                            MedicineDialogState.PictureChoose -> MedicineDialogState.PictureGrid
                            MedicineDialogState.TakePhoto, MedicineDialogState.Icons -> MedicineDialogState.PictureChoose
                            is MedicineDialogState.FullImage -> if (event.dialog.page == -1) null
                            else MedicineDialogState.FullImage(event.dialog.page)
                            else -> null
                        }
                    )
                } else {
                    it.copy(
                        dialogState = if (event.dialog is MedicineDialogState.FullImage) {
                            if (event.dialog.page == -1) null
                            else MedicineDialogState.FullImage(event.dialog.page)
                        } else {
                            event.dialog
                        }
                    )
                }
            }

            MedicineEvent.MakeDuplicate -> viewModelScope.launch {
                try {
                    val duplicate = currentState.toMedicine().copy(id = 0L)
                    val id = dao.insert(duplicate)

                    coroutineScope {
                        launch {
                            val kits = currentState.kits.map { MedicineKit(id, it.kitId) }

                            daoK.pinKit(kits)
                        }

                        launch {
                            val images = currentState.images.mapIndexed { index, image ->
                                Image(
                                    medicineId = id,
                                    position = index,
                                    image = image
                                )
                            }

                            dao.updateImages(images)
                        }
                    }

                    _action.send(MedicineAction.ShowSnackbar.OnMakeDuplicate)
                } catch (_: Exception) {
                    _action.send(MedicineAction.ShowSnackbar.OnShowError())
                }
            }
        }
    }

    fun setEditing() = updateState {
        it.copy(
            editing = true,
            default = false
        )
    }

    fun compressImages(images: List<Uri>, onCompress: suspend (Uri) -> String?) {
        viewModelScope.launch {
            updateState { it.copy(dialogState = MedicineDialogState.Loading) }

            val result = coroutineScope {
                images.map { async { onCompress(it) } }
            }

            val imageResult = result.mapNotNull { it.await() }

            val images = currentState.images.toMutableList().apply {
                addAll(imageResult)
            }

            updateState {
                it.copy(
                    images = images,
                    dialogState = MedicineDialogState.PictureGrid
                )
            }
        }
    }
}