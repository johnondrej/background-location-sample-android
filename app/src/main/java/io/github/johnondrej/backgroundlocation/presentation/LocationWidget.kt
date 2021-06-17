package io.github.johnondrej.backgroundlocation.presentation

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import io.github.johnondrej.backgroundlocation.R
import io.github.johnondrej.backgroundlocation.data.MissingPermissionsException
import io.github.johnondrej.backgroundlocation.data.NoDataException
import io.github.johnondrej.backgroundlocation.data.WidgetUpdateData
import io.github.johnondrej.backgroundlocation.data.WidgetUpdateService
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * [AppWidgetProvider] that represents location widget.
 */
class LocationWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            WidgetUpdateService.enqueueWork(context, appWidgetId)
        }
    }

    companion object {

        fun updateWidget(context: Context, appWidgetId: Int, updateData: WidgetUpdateData) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.d("LocationWidget", "updateWidget: updateData = $updateData")
                val remoteViews = RemoteViews(context.packageName, R.layout.widget_location).apply {
                    when (updateData) {
                        is WidgetUpdateData.Loaded -> {
                            setTextViewText(
                                R.id.txt_location_name,
                                context.getString(
                                    R.string.widget_location_name_format, updateData.locationName
                                )
                            )
                            setTextViewText(
                                R.id.txt_location_coords,
                                context.getString(
                                    R.string.widget_location_coords_format, updateData.location.latitude, updateData.location.longitude
                                )
                            )
                            setTextViewText(
                                R.id.txt_update_time,
                                context.getString(
                                    R.string.widget_update_time_format,
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(updateData.updateTime)
                                )
                            )
                        }
                        is WidgetUpdateData.Loading -> {
                            setTextViewText(R.id.txt_widget_status, context.getString(R.string.widget_status_loading))
                        }
                        is WidgetUpdateData.Error -> {
                            val message = when (updateData.exception) {
                                is MissingPermissionsException -> context.getString(R.string.widget_status_error_permissions)
                                is NoDataException -> context.getString(R.string.widget_status_error_location)
                                else -> context.getString(R.string.widget_status_error_general_format, updateData.exception.message)
                            }
                            setTextViewText(R.id.txt_widget_status, message)
                        }
                    }

                    setOnClickPendingIntent(
                        R.id.btn_refresh,
                        PendingIntent.getBroadcast(
                            context,
                            Random.nextInt(),
                            Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId)),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

                    setViewVisibility(R.id.layout_content, if (updateData is WidgetUpdateData.Loaded) View.VISIBLE else View.GONE)
                    setViewVisibility(R.id.btn_refresh, if (updateData !is WidgetUpdateData.Loading) View.VISIBLE else View.GONE)
                    setViewVisibility(R.id.txt_widget_status, if (updateData !is WidgetUpdateData.Loaded) View.VISIBLE else View.GONE)
                }

                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            }
        }
    }
}