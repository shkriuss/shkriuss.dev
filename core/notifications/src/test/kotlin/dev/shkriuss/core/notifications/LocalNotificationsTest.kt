package dev.shkriuss.core.notifications

import android.app.AlarmManager
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalNotificationsTest {
    @Test
    fun `rescheduleTrigger maps only expected system actions`() {
        assertEquals(
            RescheduleTrigger.BOOT_COMPLETED,
            RescheduleTrigger.fromIntentAction(Intent.ACTION_BOOT_COMPLETED),
        )
        assertEquals(
            RescheduleTrigger.TIMEZONE_CHANGED,
            RescheduleTrigger.fromIntentAction(Intent.ACTION_TIMEZONE_CHANGED),
        )
        assertEquals(
            RescheduleTrigger.PACKAGE_REPLACED,
            RescheduleTrigger.fromIntentAction(Intent.ACTION_MY_PACKAGE_REPLACED),
        )
        assertEquals(
            RescheduleTrigger.EXACT_ALARM_PERMISSION_CHANGED,
            RescheduleTrigger.fromIntentAction(
                AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            ),
        )
        assertNull(RescheduleTrigger.fromIntentAction("com.example.UNTRUSTED_ACTION"))
        assertNull(RescheduleTrigger.fromIntentAction(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `scheduledReminder rejects negative id`() {
        ScheduledReminder(id = -1L, triggerAtEpochMillis = 10_000L)
    }
}
