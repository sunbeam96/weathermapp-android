package com.weather.mapp
import android.util.Log
import org.osmdroid.util.GeoPoint
import java.net.URL
import org.json.JSONObject

class ForecastHandler {
    val forecastUrl: String = "https://api.open-meteo.com/v1/forecast?"
    private val navigation = Navigation()

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

    fun weatherTypeToString(type: WeatherType): String{
        if (type == WeatherType.SUNNY)
            return "Sunny"
        if (type == WeatherType.SNOWY)
            return "Snowy"
        if (type == WeatherType.FOGGY)
            return "Foggy"
        if (type == WeatherType.CLOUDY)
            return "Cloudy"
        else
            return "Rainy"
    }

    fun constructForecastRequest(geoPoint: GeoPoint): String{
        return forecastUrl + "latitude=" + geoPoint.latitude.toString() + "&longitude=" + geoPoint.longitude.toString() + "&current_weather=true"
    }

    fun getCurrentWeatherForLocation(geoPoint: GeoPoint): WeatherContainer {
        val meteoRequest = constructForecastRequest(geoPoint)
        val meteoApiResp = URL(meteoRequest).readText()
        Log.d("DEBUG", meteoApiResp)

        val jsonObj = JSONObject(meteoApiResp.substring(meteoApiResp.indexOf("{"), meteoApiResp.lastIndexOf("}") + 1))
        val currentWeatherJson = JSONObject(jsonObj.getString("current_weather"))
        val temp = currentWeatherJson.getString("temperature")
        val code = currentWeatherJson.getString("weathercode")
        return WeatherContainer(weatherCodeToWeatherType(code),temp.toDouble())
    }
}

