package io.github.johnondrej.backgroundlocation.data

import android.location.Location
import java.time.LocalDateTime

/**
 * Sealed class containing all possible states of widget and related data.
 */
sealed class WidgetUpdateData {

    data class Loaded(val updateTime: LocalDateTime, val location: Location, val locationName: String) : WidgetUpdateData()

    object Loading : WidgetUpdateData()

    data class Error(val exception: Exception) : WidgetUpdateData()
}