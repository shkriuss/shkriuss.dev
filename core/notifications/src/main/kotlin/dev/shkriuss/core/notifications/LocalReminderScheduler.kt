package dev.shkriuss.core.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.shkriuss.core.common.AppError
import dev.shkriuss.core.common.AppResult

/**
 * Pure on-device reminder schedule descriptor (zero FCM / Google push).
 */
data class ScheduledReminder(
    val id: Long,
    val triggerAtEpochMillis: Long,
    val exact: Boolean = true,
) {
    init {
        require(id >= 0L) { "Reminder id must be non-negative" }
        require(triggerAtEpochMillis > 0L) { "triggerAtEpochMillis must be positive" }
    }
}

/**
 * Describes what the OS currently allows for on-device reminder delivery so apps
 * can guide users calmly instead of crashing or silently failing.
 */
enum class NotificationCapability {
    EXACT_ALARMS_ALLOWED,
    INEXACT_ONLY,
    NOTIFICATIONS_DISABLED,
}

/**
 * Reasons why an app must reload and re-register pending AlarmManager reminders.
 */
enum class RescheduleTrigger {
    BOOT_COMPLETED,
    TIME_CHANGED,
    TIMEZONE_CHANGED,
    PACKAGE_REPLACED,
    EXACT_ALARM_PERMISSION_CHANGED,
    ;

    companion object {
        fun fromIntentAction(action: String?): RescheduleTrigger? = when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            -> BOOT_COMPLETED
            Intent.ACTION_TIME_CHANGED -> TIME_CHANGED
            Intent.ACTION_TIMEZONE_CHANGED -> TIMEZONE_CHANGED
            Intent.ACTION_MY_PACKAGE_REPLACED -> PACKAGE_REPLACED
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED ->
                EXACT_ALARM_PERMISSION_CHANGED
            else -> null
        }
    }
}

interface LocalReminderScheduler {
    fun currentCapability(): NotificationCapability
    fun schedule(reminder: ScheduledReminder, pendingIntent: PendingIntent): AppResult<Unit>
    fun cancel(pendingIntent: PendingIntent): AppResult<Unit>
}

/**
 * Standard Android AlarmManager scheduler surviving Doze mode while degrading
 * gracefully when exact alarm permission is revoked on Android 13+.
 */
class AndroidReminderScheduler(
    private val context: Context,
) : LocalReminderScheduler {

    private val alarmManager: AlarmManager?
        get() = context.getSystemService(AlarmManager::class.java)

    private val notificationManager: NotificationManager?
        get() = context.getSystemService(NotificationManager::class.java)

    override fun currentCapability(): NotificationCapability {
        val notificationsEnabled = notificationManager?.areNotificationsEnabled() ?: false
        if (!notificationsEnabled) {
            return NotificationCapability.NOTIFICATIONS_DISABLED
        }
        val canExact = alarmManager?.canScheduleExactAlarms() ?: false
        return if (canExact) {
            NotificationCapability.EXACT_ALARMS_ALLOWED
        } else {
            NotificationCapability.INEXACT_ONLY
        }
    }

    override fun schedule(
        reminder: ScheduledReminder,
        pendingIntent: PendingIntent,
    ): AppResult<Unit> {
        val manager = alarmManager ?: return AppResult.Failure(
            AppError.Unexpected("AlarmManager service unavailable"),
        )
        return try {
            if (reminder.exact && manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAtEpochMillis,
                    pendingIntent,
                )
            } else {
                manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAtEpochMillis,
                    pendingIntent,
                )
            }
            AppResult.Success(Unit)
        } catch (se: SecurityException) {
            AppResult.Failure(AppError.PermissionDenied("Exact alarm permission denied"))
        }
    }

    override fun cancel(pendingIntent: PendingIntent): AppResult<Unit> {
        val manager = alarmManager ?: return AppResult.Failure(
            AppError.Unexpected("AlarmManager service unavailable"),
        )
        manager.cancel(pendingIntent)
        return AppResult.Success(Unit)
    }
}

/**
 * Base receiver that filters strictly for legitimate Android reschedule broadcasts
 * before delegating to an app-specific rescheduler.
 */
abstract class BaseRescheduleReceiver : BroadcastReceiver() {
    final override fun onReceive(context: Context, intent: Intent?) {
        val trigger = RescheduleTrigger.fromIntentAction(intent?.action) ?: return
        onRescheduleRequested(context.applicationContext, trigger)
    }

    protected abstract fun onRescheduleRequested(context: Context, trigger: RescheduleTrigger)
}
