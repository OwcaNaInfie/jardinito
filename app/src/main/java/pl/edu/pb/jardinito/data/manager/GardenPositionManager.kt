package pl.edu.pb.jardinito.data.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GardenPositionsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("garden_positions", Context.MODE_PRIVATE)

    fun getPositions(gridSize: Int): Map<String, Int>? {
        val json = prefs.getString(positionsKey(gridSize), null) ?: return null
        return try {
            val obj = JSONObject(json)
            obj.keys().asSequence().associateWith { obj.getInt(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun savePositions(gridSize: Int, positions: Map<String, Int>) {
        val obj = JSONObject()
        positions.forEach { (sessionId, pos) -> obj.put(sessionId, pos) }
        prefs.edit()
            .putString(positionsKey(gridSize), obj.toString())
            .apply()
    }

    fun clearPositions() {
        prefs.edit()
            .remove(positionsKey(3))
            .remove(positionsKey(4))
            .remove(positionsKey(5))
            .remove(positionsKey(6))
            .apply()
    }

    private fun positionsKey(gridSize: Int) = "grid_${gridSize}"
}