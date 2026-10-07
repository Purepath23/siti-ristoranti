package it.purepath.promemoria

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Task(val id: Int, val title: String, val hour: Int, val minute: Int, val enabled: Boolean = true) {
    val timeLabel get() = "%02d:%02d".format(hour, minute)
}

object Store {
    private const val PREFS = "promemoria"
    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(c: Context): List<Task> {
        val arr = JSONArray(prefs(c).getString("tasks", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Task(o.getInt("id"), o.getString("title"), o.getInt("h"), o.getInt("m"), o.optBoolean("on", true))
        }.sortedWith(compareBy({ it.hour }, { it.minute }))
    }

    fun get(c: Context, id: Int) = load(c).firstOrNull { it.id == id }

    private fun save(c: Context, tasks: List<Task>) {
        val arr = JSONArray()
        tasks.forEach {
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("h", it.hour).put("m", it.minute).put("on", it.enabled))
        }
        prefs(c).edit().putString("tasks", arr.toString()).apply()
    }

    fun add(c: Context, title: String, hour: Int, minute: Int): Task {
        val p = prefs(c)
        val id = p.getInt("next_id", 1)
        p.edit().putInt("next_id", id + 1).apply()
        val t = Task(id, title, hour, minute)
        save(c, load(c) + t)
        return t
    }

    fun update(c: Context, t: Task) = save(c, load(c).map { if (it.id == t.id) t else it })
    fun remove(c: Context, id: Int) = save(c, load(c).filter { it.id != id })
}
