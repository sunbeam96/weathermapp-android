package com.weather.mapp
import android.util.Log

class WeatherContainer(type: WeatherType, temperature: Double)
{
    private var temp: Double = 0.0
    private var weathertype: WeatherType = WeatherType.SUNNY

    init {
        Log.d("DEBUG", "Filling weather container with temp: " + temperature.toString())
        temp = temperature
        weathertype = type
    }

    fun getTemp(): Double {
        return temp
    }

    fun getType(): WeatherType {
        return weathertype
    }
}
