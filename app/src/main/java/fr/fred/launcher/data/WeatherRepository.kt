package fr.fred.launcher.data

import fr.fred.launcher.model.WeatherState
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class WeatherRepository {
    // Emplacement météo V1. À remplacer ensuite par le réglage utilisateur ou la localisation Android.
    private val locationName = "Gardanne"
    private val latitude = 43.4549
    private val longitude = 5.4691

    fun fetch(): WeatherState {
        val endpoint = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$latitude&longitude=$longitude&current=temperature_2m,weather_code&timezone=auto"

        return runCatching {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4_000
                readTimeout = 4_000
                requestMethod = "GET"
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val current = JSONObject(body).getJSONObject("current")
            val temp = current.getDouble("temperature_2m").roundToInt()
            val code = current.getInt("weather_code")
            WeatherState(
                location = locationName,
                temperatureC = temp,
                description = codeToDescription(code),
                weatherCode = code,
                isLoading = false,
            )
        }.getOrElse {
            WeatherState(
                location = locationName,
                temperatureC = null,
                description = "Météo indisponible",
                isLoading = false,
                error = true,
            )
        }
    }

    private fun codeToDescription(code: Int): String = when (code) {
        0 -> "Ciel dégagé"
        1 -> "Plutôt dégagé"
        2 -> "Partiellement nuageux"
        3 -> "Couvert"
        45, 48 -> "Brouillard"
        51, 53, 55, 56, 57 -> "Bruine"
        61, 63, 65, 66, 67 -> "Pluie"
        71, 73, 75, 77 -> "Neige"
        80, 81, 82 -> "Averses"
        85, 86 -> "Averses de neige"
        95, 96, 99 -> "Orage"
        else -> "Conditions variables"
    }
}
