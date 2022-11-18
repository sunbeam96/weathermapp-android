package com.weather.mapp

import android.location.Address
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private var okHttpClient = OkHttpClient()
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var navigationView: DrawerLayout
    private lateinit var osmdroidMap: MapView
    private lateinit var roadManager: RoadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Configuration.getInstance().userAgentValue = "application/1.0"

        navigationMenu()
        hideActionBar()
    }

    private fun hideActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()
    }

    private fun navigationMenu() {
        navigationView = findViewById(R.id.drawer_layout)
        navigationView.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT)

        osmdroidMap = findViewById(R.id.map)
        osmdroidMap.setTileSource(TileSourceFactory.MAPNIK)
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), osmdroidMap)
        roadManager = OSRMRoadManager(this, Configuration.getInstance().userAgentValue)

        myLocationOverlay.enableMyLocation()
        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                osmdroidMap.controller.animateTo(myLocationOverlay.myLocation)
                osmdroidMap.controller.setZoom(20)
            }
        }
    }

    fun openNavigationView(view: View) {
        if (navigationView.isDrawerOpen(Gravity.START)) {
            navigationView.closeDrawer(Gravity.START)
        } else {
            navigationView.openDrawer(Gravity.START)
        }
    }

    fun routeSearch(view: View) {
        val destinationPoint: EditText = findViewById(R.id.destination_point)
        val startPoint: EditText = findViewById(R.id.start_point)
        val startPointAddress = findCoordinates(startPoint.text.toString())
        val destinationPointAddress = findCoordinates(destinationPoint.text.toString())
        val startPointGeoPoint = GeoPoint(startPointAddress.latitude, startPointAddress.longitude)
        val destinationPointGeoPoint =
            GeoPoint(destinationPointAddress.latitude, destinationPointAddress.longitude)
        val geoPointList = ArrayList<GeoPoint>()
        geoPointList.add(startPointGeoPoint)
        geoPointList.add(destinationPointGeoPoint)

        thread(start = true) {
            val road = roadManager.getRoad(geoPointList)
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            osmdroidMap.overlays.clear()
            osmdroidMap.overlays.add(roadOverlay)
            osmdroidMap.invalidate()
        }

        osmdroidMap.controller.animateTo(destinationPointGeoPoint)
        osmdroidMap.controller.setZoom(7.5)
    }

    private fun findCoordinates(placeName: String): Address {
        val requestUrl =
            "https://nominatim.openstreetmap.org/search.php?format=json&limit=1&q=$placeName"
        val nominatimRequest = Request.Builder().url(requestUrl).build()
        val address = Address(Locale.ENGLISH)
        val countDownLatch = CountDownLatch(1)

        thread(start = true) {
            var nominatimResponse = ""

            okHttpClient.newCall(nominatimRequest).execute().use { response ->
                nominatimResponse =
                    response.body?.string() ?: ""
            }

            val jsonElement = JsonParser.parseString(nominatimResponse).asJsonArray[0]
            address.latitude = jsonElement.asJsonObject.get("lat").asDouble
            address.longitude = jsonElement.asJsonObject.get("lon").asDouble
            countDownLatch.countDown()
        }

        countDownLatch.await()

        return address
    }
}