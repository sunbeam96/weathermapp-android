package com.weather.mapp

import android.location.Address
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.util.GeoPoint
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class Navigation {
    private var okHttpClient = OkHttpClient()
    private val openStritLinkApi =
        "https://nominatim.openstreetmap.org/search.php?format=json&limit=1&q="

    fun findCoordinates(placeName: String): Address {
        val (nominatimRequest, address, countDownLatch) = prepareRequest(placeName)
        newCall(nominatimRequest, address, countDownLatch)
        countDownLatch.await()
        return address
    }

    private fun prepareRequest(placeName: String): Triple<Request, Address, CountDownLatch> {
        val requestUrl = openStritLinkApi + placeName
        val nominatimRequest = Request.Builder().url(requestUrl).build()
        val address = Address(Locale.ENGLISH)
        val countDownLatch = CountDownLatch(1)
        return Triple(nominatimRequest, address, countDownLatch)
    }

    private fun newCall(
        nominatimRequest: Request,
        address: Address,
        countDownLatch: CountDownLatch
    ) {
        thread(start = true) {
            var nominatimResponse = ""

                okHttpClient.newCall(nominatimRequest).execute().use { response ->
                    nominatimResponse =
                        response.body!!.string() ?: ""
                }

                getLocalization(nominatimResponse, address, countDownLatch)
        }
    }

    private fun getLocalization(
        nominatimResponse: String,
        address: Address,
        countDownLatch: CountDownLatch
    ) {
        val jsonElement = JsonParser.parseString(nominatimResponse).asJsonArray[0]
        address.latitude = jsonElement.asJsonObject.get("lat").asDouble
        address.longitude = jsonElement.asJsonObject.get("lon").asDouble
        countDownLatch.countDown()
    }

    fun pair(
        startPointAddress: Address,
        destinationPointAddress: Address
    ): Pair<GeoPoint, ArrayList<GeoPoint>> {
        val startPointGeoPoint = GeoPoint(startPointAddress.latitude, startPointAddress.longitude)
        val destinationPointGeoPoint =
            GeoPoint(destinationPointAddress.latitude, destinationPointAddress.longitude)
        val geoPointList = ArrayList<GeoPoint>()
        geoPointList.add(startPointGeoPoint)
        geoPointList.add(destinationPointGeoPoint)
        return Pair(destinationPointGeoPoint, geoPointList)
    }
}