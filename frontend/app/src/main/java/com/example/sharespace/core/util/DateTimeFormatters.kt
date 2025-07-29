import android.content.Context
import android.text.format.DateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

fun displayFormatter(context: Context): DateTimeFormatter {
    val pattern = if (DateFormat.is24HourFormat(context)) {
        "MMMM d | HH:mm"
    } else {
        "MMMM d | h:mm a"
    }
    return DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
}

/** Parse ISO local date-time string (no zone) and treat it as UTC, then show in user's zone. */
fun formatIsoAssumingUtc(iso: String, context: Context): String {
    val ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val zoned = ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault())
    return displayFormatter(context).format(zoned)
}

/** If you already have a LocalDateTime, also assume it is UTC. */
fun formatUtc(ldt: LocalDateTime, context: Context): String {
    val zoned = ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault())
    return displayFormatter(context).format(zoned)
}

/** Accept either String (ISO) or LocalDateTime. */
fun displayDateFlexible(value: Any, context: Context): String = when (value) {
    is String -> formatIsoAssumingUtc(value, context)
    is LocalDateTime -> formatUtc(value, context)
    is Instant -> displayFormatter(context).format(value.atZone(ZoneId.systemDefault()))
    else -> value.toString()
}
