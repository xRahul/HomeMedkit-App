package `in`.rahulja.medicinekit.utils.extensions

import `in`.rahulja.medicinekit.utils.BLANK
import java.io.File
import java.security.MessageDigest

suspend fun File.md5(): String {
    val md = MessageDigest.getInstance("MD5")

    return inputStream().use { inputStream ->
        val buffer = ByteArray(8192)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            md.update(buffer, 0, bytesRead)
            bytesRead = inputStream.read(buffer)
        }

        md.digest().joinToString(BLANK) { "%02x".format(it) }
    }
}