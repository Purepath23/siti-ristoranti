package it.purepath.promemoria

import android.app.NotificationManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {
    private var taskId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_alarm)
        bind()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        bind()
    }

    private fun bind() {
        taskId = intent.getIntExtra("id", -1)
        val task = Store.get(this, taskId)
        if (task == null) { finish(); return }
        findViewById<TextView>(R.id.clock).text = task.timeLabel
        findViewById<TextView>(R.id.title).text = task.title

        findViewById<Button>(R.id.btnDone).setOnClickListener { stopAlarm(); finish() }
        mapOf(R.id.s5 to 5, R.id.s10 to 10, R.id.s30 to 30, R.id.s60 to 60).forEach { (b, m) ->
            findViewById<Button>(b).setOnClickListener { snooze(m) }
        }
        findViewById<Button>(R.id.btnCustom).setOnClickListener {
            val m = findViewById<EditText>(R.id.custom).text.toString().toIntOrNull()
            if (m != null && m > 0) snooze(m) else findViewById<EditText>(R.id.custom).error = "Inserisci i minuti"
        }
    }

    private fun stopAlarm() = getSystemService(NotificationManager::class.java).cancel(taskId)

    private fun snooze(minutes: Int) {
        stopAlarm()
        Scheduler.snooze(this, taskId, minutes)
        finish()
    }
}
