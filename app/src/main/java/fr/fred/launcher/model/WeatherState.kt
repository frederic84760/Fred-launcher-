package fr.fred.launcher.model

data class WeatherState(
    val location: String = "Gardanne",
    val temperatureC: Int? = null,
    val description: String = "Chargement…",
    val weatherCode: Int? = null,
    val isLoading: Boolean = true,
    val error: Boolean = false,
)
