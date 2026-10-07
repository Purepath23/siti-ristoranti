package it.purepath.promemoria

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, intent: Intent) = Scheduler.scheduleAll(c)
}
