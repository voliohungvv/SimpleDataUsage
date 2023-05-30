import android.os.StatFs
import android.telephony.PhoneNumberUtils
import java.text.Normalizer
import java.util.*

// remove diacritics, for example č -> c
val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun String.normalizeString() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(normalizeRegex, "")

fun String.normalizePhoneNumber() = PhoneNumberUtils.normalizeNumber(this)

fun String.getNameLetter() = normalizeString().toCharArray().getOrNull(0)?.toString()?.toUpperCase(
    Locale.getDefault()) ?: "A"