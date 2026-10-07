package it.purepath.promemoria

import android.Manifest
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AlarmReceiver.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { addTask() }
        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<Button>(R.id.btnOverlay).visibility =
            if (Settings.canDrawOverlays(this)) View.GONE else View.VISIBLE
        if (Build.VERSION.SDK_INT >= 34 &&
            !getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        ) startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName")))
        render()
    }

    private fun render() {
        val list = findViewById<LinearLayout>(R.id.list)
        list.removeAllViews()
        val tasks = Store.load(this)
        findViewById<View>(R.id.empty).visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        tasks.forEach { t ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_task, list, false)
            row.findViewById<TextView>(R.id.time).text = t.timeLabel
            row.findViewById<TextView>(R.id.title).text = t.title
            row.findViewById<MaterialSwitch>(R.id.toggle).apply {
                isChecked = t.enabled
                setOnCheckedChangeListener { _, on ->
                    val u = t.copy(enabled = on); Store.update(this@MainActivity, u); Scheduler.schedule(this@MainActivity, u)
                }
            }
            row.setOnClickListener { editTask(t) }
            row.setOnLongClickListener { confirmDelete(t); true }
            list.addView(row)
        }
    }

    private fun addTask() = editTask(null)

    private fun editTask(existing: Task?) {
        val input = EditText(this).apply { hint = "Es. Prendere magnesio"; setText(existing?.title ?: "") }
        val pad = (20 * resources.displayMetrics.density).toInt()
        val box = LinearLayout(this).apply { setPadding(pad, pad / 2, pad, 0); addView(input) }
        val builder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Nuovo promemoria" else "Modifica promemoria")
            .setView(box)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Scegli orario") { _, _ ->
                val title = input.text.toString().trim().ifEmpty { "Promemoria" }
                TimePickerDialog(this, { _, h, m ->
                    if (existing == null) {
                        Scheduler.schedule(this, Store.add(this, title, h, m))
                    } else {
                        val u = existing.copy(title = title, hour = h, minute = m, enabled = true)
                        Store.update(this, u); Scheduler.schedule(this, u)
                    }
                    render()
                }, existing?.hour ?: 9, existing?.minute ?: 0, true).show()
            }
        if (existing != null) builder.setNeutralButton("Elimina") { _, _ -> confirmDelete(existing) }
        builder.show()
    }

    private fun confirmDelete(t: Task) {
        AlertDialog.Builder(this).setTitle("Eliminare \"${t.title}\"?")
            .setNegativeButton("No", null)
            .setPositiveButton("Elimina") { _, _ -> Scheduler.cancel(this, t.id); Store.remove(this, t.id); render() }
            .show()
    }
}
