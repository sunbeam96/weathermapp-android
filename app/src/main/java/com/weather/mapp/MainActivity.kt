package com.weather.mapp

import android.location.Address
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private val navigation = Navigation()
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
        val startPointAddress = navigation.findCoordinates(startPoint.text.toString())
        val destinationPointAddress = navigation.findCoordinates(destinationPoint.text.toString())
        generateRoadTrace(startPointAddress, destinationPointAddress)
    }

    private fun generateRoadTrace(
        startPointAddress: Address,
        destinationPointAddress: Address
    ) {
        val (destinationPointGeoPoint, geoPointList) = navigation.pair(
            startPointAddress,
            destinationPointAddress
        )

        getRoad(geoPointList, destinationPointGeoPoint)
    }

    private fun getRoad(
        geoPointList: ArrayList<GeoPoint>,
        destinationPointGeoPoint: GeoPoint
    ) {
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
}