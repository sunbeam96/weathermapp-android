package com.weather.mapp

import android.app.AlertDialog
import android.content.Intent
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
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.concurrent.thread
import android.util.Log
import androidx.core.view.GravityCompat
import com.weather.mapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val navigation = Navigation()
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var navigationView: DrawerLayout
    private lateinit var osmdroidMap: MapView
    private lateinit var roadManager: RoadManager
    private var startingPoint: String = ""
    private var endingPoint: String = ""
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        Configuration.getInstance().userAgentValue = "application/1.0"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent = Intent(this, MainActivity2::class.java)

        navigationMenu()
        hideActionBar()


        with(binding){
            hamburgerButton.setOnClickListener{
                navigationView.openDrawer(GravityCompat.START)
            }
        }

        binding.apply {
            navView.setNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.account -> {}
                    R.id.authors -> {
                        startActivity(intent)
                    }
                    R.id.licences -> {}
                    R.id.about -> {}
                }
                true
            }
        }
    }

    private fun hasLocationIllegalCharacters(location: String): Boolean{
        for (character in location){
            if (character !in 'A'..'Z' || character !in 'a'..'z' || character != ' ')
                return true
        }
        return false
    }
    
    private fun hideActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()
    }

    private fun navigationMenu() {
        navigationView = findViewById(R.id.drawer_layout)
//        navigationView.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT)

        osmdroidMap = findViewById(R.id.map)
        osmdroidMap.setTileSource(TileSourceFactory.MAPNIK)
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), osmdroidMap)
        roadManager = OSRMRoadManager(this, Configuration.getInstance().userAgentValue)

        val startLocation = GeoPoint(52.23, 21.01)
        myLocationOverlay.enableMyLocation()
        osmdroidMap.controller.animateTo(startLocation)
        osmdroidMap.controller.setZoom(4.5)

        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                osmdroidMap.controller.animateTo(myLocationOverlay.myLocation)
                osmdroidMap.controller.setZoom(2.5)
            }
        }
    }

    /*fun showAbout(view: View){
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("About")
        alertBuilder.setMessage("WWSIS school project by Grzegorz Kędzior @grzesiekkedzior, Dawid Żwikiewicz @S1NNR916,\n" +
                "Marek Borowski @maro4444, Kuba Łukaszczyk @sunbeam96")
        alertBuilder.show()
    }*/

    /*fun openNavigationView(view: View) {
        if (navigationView.isDrawerOpen(GravityCompat.START)) {
            navigationView.closeDrawer(GravityCompat.START)
        } else {
            navigationView.openDrawer(GravityCompat.START)
        }
    }
*/
    fun routeSearch(view: View) {
        Log.d("DEBUG", "Running routeSearch")
        val destinationPoint: EditText = findViewById(R.id.destination_point)
        val startPoint: EditText = findViewById(R.id.start_point)
        startingPoint = startPoint.text.toString()
        endingPoint = destinationPoint.text.toString()
        if (startingPoint != "" && endingPoint != "") {
            Log.d("DEBUG", "Chosen start: " + startingPoint)
            Log.d("DEBUG", "Chosen finish: " + endingPoint)
            val startPointAddress = navigation.findCoordinates(startingPoint)
            val destinationPointAddress = navigation.findCoordinates(endingPoint)
            generateRoadTrace(startPointAddress, destinationPointAddress)
        }

    }

    private fun generateRoadTrace(
        startPointAddress: Address,
        destinationPointAddress: Address
    ) {
        val (destinationPointGeoPoint, geoPointList) = navigation.pair(
            startPointAddress,
            destinationPointAddress
        )
        Log.d("DEBUG", "Generating road trace")
        getRoad(geoPointList, destinationPointGeoPoint)
    }

    private fun getRoad(
        geoPointList: ArrayList<GeoPoint>,
        destinationPointGeoPoint: GeoPoint
    ) {
        Log.d("DEBUG", "Clearing overlays")
        osmdroidMap.overlays.clear()

        Thread(Runnable {
            Log.d("DEBUG", "Running in roadmanager.getRoad thread")

            val road = roadManager.getRoad(geoPointList)
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            osmdroidMap.overlays.add(roadOverlay)
            Log.d("DEBUG", "Map invalidate()")

            osmdroidMap.invalidate()
        }).start()
        Thread(Runnable {
            // GET WEATHER FOR ROAD
            Log.d("DEBUG", "Running in ForecastHandler thread")

            var weatherHandler = ForecastHandler()
            val startWeather = weatherHandler.getCurrentWeatherForLocation(geoPointList.get(0))
            val endWeather = weatherHandler.getCurrentWeatherForLocation(geoPointList.get(1))
            val endArrivalWeather = weatherHandler.getWeatherForLocationAfterGivenTime(geoPointList.get(1), roadManager.getRoad(geoPointList).mDuration)
            val startWeatherMarker = Marker(osmdroidMap)
            startWeatherMarker.position = geoPointList.get(0)
            startWeatherMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            startWeatherMarker.setTitle("Now: " + startWeather.getTemp().toString() + "°C " + weatherHandler.weatherTypeToString(startWeather.getType()));
            osmdroidMap.overlays.add(startWeatherMarker)

            val endWeatherMarker = Marker(osmdroidMap)
            endWeatherMarker.position = geoPointList.get(1)
            endWeatherMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            endWeatherMarker.setTitle("Now:" + endWeather.getTemp().toString() + "°C " + weatherHandler.weatherTypeToString(endWeather.getType()) + "\n"
                + "At arrival: " + endArrivalWeather.getTemp().toString() + "°C "+ weatherHandler.weatherTypeToString(endArrivalWeather.getType()));
            osmdroidMap.overlays.add(endWeatherMarker)

            osmdroidMap.invalidate()
        }).start()
        Log.d("DEBUG", "Map controller animations and zoom")
        osmdroidMap.controller.animateTo(destinationPointGeoPoint)
        osmdroidMap.controller.setZoom(7.5)
        osmdroidMap.invalidate()

    }



}