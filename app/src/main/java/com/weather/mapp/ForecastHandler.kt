package com.weather.mapp
import com.openmeteo.api.OpenMeteo
import com.openmeteo.api.common.units.TemperatureUnit

class ForecastHandler {

    private fun weatherCodeToWeatherType(code: String): WeatherType {
        if (code.toInt() == 0)
            return WeatherType.SUNNY
        if (code.toInt() == 71
            || code.toInt() == 73|| code.toInt() == 75
            || code.toInt() == 77 || code.toInt() == 85 || code.toInt() == 86)
            return WeatherType.SNOWY
        if (code.toInt() == 45 || code.toInt() == 48)
            return WeatherType.FOGGY
        if (code.toInt() == 1 || code.toInt() == 2 || code.toInt() == 3)
            return WeatherType.CLOUDY
        else
            return WeatherType.RAINY
    }

    fun getCurrentWeatherForLocation(placeName: String): WeatherContainer {
        val openMeteoResp = OpenMeteo(placeName)
            .currentWeather(TemperatureUnit.Celsius)
            .getOrNull()

        var tempContainer: WeatherContainer = WeatherContainer(WeatherType.SUNNY,0.0)
        openMeteoResp?.run {
            currentWeather?.run {
                val temp = "$temperature".toDouble()
                println("DEBUG TEMP $temperature")
                val weathercode = "$weatherCode"
                return WeatherContainer(weatherCodeToWeatherType(weathercode), temp)
            }
        }
        return tempContainer
    }
}

