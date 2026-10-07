package it.purepath.promemoria

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_FIRE = "it.purepath.promemoria.FIRE"
        const val ACTION_DONE = "it.purepath.promemoria.DONE"
        const val ACTION_SNOOZE = "it.purepath.promemoria.SNOOZE"
        private const val CHANNEL = "reminders_v1"

        fun ensureChannel(c: Context) {
            val nm = c.getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL) != null) return
            val ch = NotificationChannel(CHANNEL, "Promemoria", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 400, 600, 400, 600)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(ch)
        }
    }

    override fun onReceive(c: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val nm = c.getSystemService(NotificationManager::class.java)
        when (intent.action) {
            ACTION_DONE -> nm.cancel(id)
            ACTION_SNOOZE -> {
                nm.cancel(id)
                Scheduler.snooze(c, id, intent.getIntExtra("minutes", 10))
            }
            ACTION_FIRE -> fire(c, id, intent.getBooleanExtra("snooze", false))
        }
    }

    private fun fire(c: Context, id: Int, isSnooze: Boolean) {
        val task = Store.get(c, id) ?: return
        // riprogramma la prossima occorrenza giornaliera (non per le ripetizioni "posticipa")
        if (!isSnooze) Scheduler.schedule(c, task, minAhead = 60 * 60_000L)
        ensureChannel(c)

        val full = PendingIntent.getActivity(
            c, id, Intent(c, AlarmActivity::class.java).putExtra("id", id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        fun action(a: String, minutes: Int, req: Int) = PendingIntent.getBroadcast(
            c, id * 10 + req,
            Intent(c, AlarmReceiver::class.java).setAction(a).setData(Uri.parse("act://$id/$a"))
                .putExtra("id", id).putExtra("minutes", minutes),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(task.title)
            .setContentText("Promemoria delle ${task.timeLabel}")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(full)
            .setFullScreenIntent(full, true)
            .addAction(0, "Fatto", action(ACTION_DONE, 0, 1))
            .addAction(0, "Posticipa 10 min", action(ACTION_SNOOZE, 10, 2))
            .build()
        n.flags = n.flags or android.app.Notification.FLAG_INSISTENT
        nm(c).notify(id, n)

        // a telefono sbloccato il full-screen intent diventa solo heads-up: se l'utente ha concesso
        // "mostra sopra altre app" apriamo direttamente la schermata a tutto schermo
        if (Settings.canDrawOverlays(c)) {
            runCatching {
                c.startActivity(Intent(c, AlarmActivity::class.java).putExtra("id", id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
        }
    }

    private fun nm(c: Context) = c.getSystemService(NotificationManager::class.java)
}
