package com.weather.mapp

class WeatherContainer(type: WeatherType, temperature: Double)
{
    private var temp: Double = 0.0
    private var weathertype: WeatherType = WeatherType.SUNNY

    init {
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
