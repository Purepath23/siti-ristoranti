package it.purepath.promemoria

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Calendar

object Scheduler {
    fun nextTrigger(t: Task, minAhead: Long = 0): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, t.hour); set(Calendar.MINUTE, t.minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        while (cal.timeInMillis <= System.currentTimeMillis() + minAhead) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    private fun pending(c: Context, id: Int, snooze: Boolean): PendingIntent {
        val i = Intent(c, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ACTION_FIRE)
            .setData(Uri.parse((if (snooze) "snooze://" else "task://") + id))
            .putExtra("id", id).putExtra("snooze", snooze)
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun setAt(c: Context, at: Long, pi: PendingIntent) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val show = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), pi)
    }

    fun schedule(c: Context, t: Task, minAhead: Long = 0) {
        if (!t.enabled) { cancel(c, t.id); return }
        setAt(c, nextTrigger(t, minAhead), pending(c, t.id, false))
    }

    fun snooze(c: Context, id: Int, minutes: Int) =
        setAt(c, System.currentTimeMillis() + minutes * 60_000L, pending(c, id, true))

    fun cancel(c: Context, id: Int) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(c, id, false)); am.cancel(pending(c, id, true))
    }

    fun scheduleAll(c: Context) = Store.load(c).forEach { schedule(c, it) }
}
